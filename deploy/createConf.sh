#!/bin/bash

IP=`awk 'NR==1 {print $1}' /etc/hosts`
PORT=2551

/bin/java -jar hajp-orchestrator.jar $IP $PORT
