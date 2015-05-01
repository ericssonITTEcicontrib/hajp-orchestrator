## Goal

Building an orchestrator and frontend for cluster health visualization

* A cluster state dashboard

## Getting Started

## Setup Activator
Download activator from TypeSafe or use activator bash script.

Run each line in a new terminal.

```
activator "project orchestrator" "run 2551"
mvn hpi:run (ensure port is not clashing and change cluster system to HajpCluster from ClusterSystem)

```

## HAProxy setup
HAJP orchestrator proxy requires HAProxy to be installed and available to running user.
  for Mac OS: $ brew update
              $ brew install haproxy

  for CentOS: $ yum install haproxy

  for Ubuntu: $ apt-get update
              $ apt-get install haproxy

Also it assumes the existence of an environmental variable called orchestrator_deploy which
should point to absolute location of orchestrator/ folder.

Underneath orchestrator folder, there exists a folder named conf which contains two files:

    haproxy.cfg which is the actively modified run-time ha proxy config file
    haproxy.pid which is the actively modified run-time process id file
    Both of these files need to be maintained to ensure userid etc. match system deployment settings

Proxy feature is fully implemented on port 5000 for testing purposes, modification of sample haproxy.conf
is highly advised for production run.





