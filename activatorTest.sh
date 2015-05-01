#!/bin/bash
activator test -Dhttp.proxyHost=www-proxy.lmc.ericsson.se -Dhttp.proxyPort=8080 -Dhttps.proxyHost=www-proxy.lmc.ericsson.se -Dhttps.proxyPort=8080 -Dhttp.nonProxyHosts="*.ericsson.se"
