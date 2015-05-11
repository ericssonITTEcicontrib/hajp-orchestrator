#!/bin/bash -e


if [ -z "$1" ]
  then
    echo "No arguments supplied"
    exit 1
fi

set -e


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
  SNAPSHOTVERSION="$major.$minor.$micro"
else
  ## If not -SNAPSHOT in version
  ## Then the release version is the version.
  VERSION=${VERSIONSTR:25:$FINALCHARPOS}
  RELEASEVERSION=$VERSION
fi

RELEASEURL=https://oss.sonatype.org/content/repositories/releases/com/ericsson/jenkinsci/hajp/hajp-orchestrator_2.11/$RELEASEVERSION/hajp-orchestrator_2.11-$RELEASEVERSION-assembly.jar

echo $SNAPSHOTURL

# Download orchestrator
if [ $1 == "release" ]
  then
    wget --no-proxy -O hajp-orchestrator.jar --user=$USERNAME --password=$PASSWD $RELEASEURL
fi
