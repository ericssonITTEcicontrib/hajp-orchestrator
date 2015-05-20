package com.ericsson.jenkinsci.hajp.orchestrator

import akka.actor.ActorSystem
import com.ericsson.jenkinsci.hajp.orchestrator.orchestrator.OrchestratorBackend
import com.google.inject.{AbstractModule, Guice}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

object OrchestratorRunner {
  def run(module: AbstractModule): Unit = {
    val injector = Guice.createInjector(
      module
    )

    val system = injector.instance[ActorSystem]

    // Deploy actors and services
    OrchestratorBackend startOn system
  }
}
