package com.ericsson.jenkinsci.hajp.orchestrator

import java.util

import akka.actor.ActorSystem
import com.ericsson.jenkinsci.hajp.orchestrator.orchestrator.OrchestratorBackend
import com.typesafe.config.{ConfigValueFactory, ConfigFactory}
import collection.JavaConversions._
import scala.collection.JavaConversions._

/**
 * Booting a cluster backend node with all actors
 */
object Orchestrator extends App {

  // Simple cli parsing
  if(args.length < 1)
    throw new IllegalArgumentException(s"Both hostname and ports are required Args [ $args ] are invalid")
  val hostname = args(0)
  val port = args(1)
  val seedNodes: java.util.List[String] = new util.ArrayList()
  seedNodes.add("akka.tcp://HajpCluster@"+hostname+":"+port)
  // System initialization
  val properties = Map(
    "akka.remote.netty.tcp.port" -> port,
  "akka.remote.netty.tcp.hostname" -> hostname,
  "akka.cluster.seed-nodes" -> seedNodes
  )

  val system = ActorSystem("HajpCluster", (ConfigFactory parseMap properties)
    .withFallback(ConfigFactory.load())
  )

  // Deploy actors and services
  OrchestratorBackend startOn system
}
