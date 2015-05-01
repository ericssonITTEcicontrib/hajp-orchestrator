#!/bin/bash

IP=`ifconfig eth0 | grep 'inet addr' | cut -d: -f2 | awk '{print $1}'`

activator "project orchestrator" "run $IP 2551" -Dhttp.proxyHost=www-proxy.lmc.ericsson.se -Dhttp.proxyPort=8080 -Dhttps.proxyHost=www-proxy.lmc.ericsson.se -Dhttps.proxyPort=8080 -Dhttp.nonProxyHosts="*.ericsson.se"
