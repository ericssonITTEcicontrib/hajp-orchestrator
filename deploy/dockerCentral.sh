#!/bin/bash -e

PROJECT=proj_hajp
REGISTRY=armdocker.rnd.ericsson.se
SUBPROJECT=orchestrator
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
    docker build --no-cache=true -t $REGISTRY/$PROJECT/$SUBPROJECT:$RELEASEVERSION .
    rm -f *.jar
fi

if [ $1 == "buildSnapshot" ]
  then
    ./packageOrchestrator.sh snapshot
    docker build --no-cache=true -t $REGISTRY/$PROJECT/$SUBPROJECT:SNAPSHOT .
    rm -f *.jar
fi

if [ $1 == "runRelease" ]
  then
   	docker run -p 9000:9000 $REGISTRY/$PROJECT/$SUBPROJECT:$RELEASEVERSION
fi

if [ $1 == "runSnapshot" ]
  then
   	docker run -p 9000:9000 $REGISTRY/$PROJECT/$SUBPROJECT:SNAPSHOT
fi

if [ $1 == "pushRelease" ]
  then
    docker push $REGISTRY/$PROJECT/$SUBPROJECT:$RELEASEVERSION
fi

if [ $1 == "pushSnapshot" ]
  then
    docker push $REGISTRY/$PROJECT/$SUBPROJECT:SNAPSHOT
fi

