#!/bin/bash -e

PROJECT=hajp-orchestrator
REGISTRY=docker.io/ericssonitte
VERSIONSTR=$(head -n 1 ../version.sbt)
SNAPSHOTBEGIN=`echo $VERSIONSTR | grep -b -o '-' | awk 'BEGIN {FS=":"}{print $1}' | bc`
SNAPSHOTBEGIN=$((SNAPSHOTBEGIN - 25))
STRSIZE=${#VERSIONSTR}

FINALCHARPOS=$((STRSIZE - 25 - 1))

VERSIONSTR=$(head -n 1 ../version.sbt)
SNAPSHOTBEGIN=`echo $VERSIONSTR | grep -b -o '-' | awk 'BEGIN {FS=":"}{print $1}' | bc`
SNAPSHOTBEGIN=$((SNAPSHOTBEGIN - 25))
STRSIZE=${#VERSIONSTR}

FINALCHARPOS=$((STRSIZE - 25 - 1))

if [[ (( "$SNAPSHOTBEGIN" -gt 0 )) ]]; then
  ## If version has -SNAPSHOT in it, then the
  ## release version should have its micro 1 less
  ## than the snapshot
  ## Example: version in version.sbt = 1.0.2-SNAPSHOT
  ##          release version is 1.0.1
  VERSION=${VERSIONSTR:25:$SNAPSHOTBEGIN}
  major=$(echo $VERSION | cut -d. -f1)
  minor=$(echo $VERSION | cut -d. -f2)
  micro=$(echo $VERSION | cut -d. -f3)
  releasemicro=$(echo "$micro - 1" | bc)
  RELEASEVERSION="$major.$minor.$releasemicro"
  SNAPSHOTVERSION="$major.$minor.$micro-SNAPSHOT"
else
  ## If not -SNAPSHOT in version
  ## Then the release version is the version.
  VERSION=${VERSIONSTR:25:$FINALCHARPOS}
  RELEASEVERSION=$VERSION
fi

if [ -z "$1" ]
  then
    echo "No arguments supplied"
    exit 1
fi

set -e

export http_proxy=
export https_proxy=

if [ $1 == "buildRelease" ]
  then
    ./packageOrchestrator.sh release
    docker build --no-cache=true -t $REGISTRY/$PROJECT:$RELEASEVERSION .
    rm -f *.jar
fi

if [ $1 == "runRelease" ]
  then
   	docker run -p 9000:9000 $REGISTRY/$PROJECT:$RELEASEVERSION
fi

if [ $1 == "pushRelease" ]
  then
    docker tag -f $REGISTRY/$PROJECT:$RELEASEVERSION $REGISTRY/$PROJECT:latest
    docker push $REGISTRY/$PROJECT:$RELEASEVERSION
    docker push $REGISTRY/$PROJECT:latest
fi

