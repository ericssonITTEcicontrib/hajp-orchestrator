package com.ericsson.jenkinsci.hajp.orchestrator

import java.util

import com.ericsson.jenkinsci.hajp.orchestrator.akkaguice.AkkaModule
import com.google.inject.AbstractModule
import com.typesafe.config.{Config, ConfigFactory}

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

  val config: Config = (ConfigFactory parseMap properties).withFallback(ConfigFactory.load())

  val module:AbstractModule = new AkkaModule("HajpCluster", config)

  OrchestratorRunner.run(module)
}
