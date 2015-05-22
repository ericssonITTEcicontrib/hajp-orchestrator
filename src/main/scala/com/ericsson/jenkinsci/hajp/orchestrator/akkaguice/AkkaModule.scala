package com.ericsson.jenkinsci.hajp.orchestrator.akkaguice

import javax.inject.Inject

import akka.actor.ActorSystem
import com.ericsson.jenkinsci.hajp.orchestrator.akkaguice.AkkaModule.ActorSystemProvider
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Injector, Provider}
import com.typesafe.config.Config
import net.codingwell.scalaguice.InjectorExtensions._
import net.codingwell.scalaguice.ScalaModule

object AkkaModule {
  class ActorSystemProvider @Inject() (val injector: Injector) extends Provider[ActorSystem] {
    override def get() = {
      val name:String  = injector.instance[String](Names.named("name"))
      val config:Config = injector.instance[Config]
      val system = ActorSystem(name, config)
      system
    }
  }
}

/**
 * A module providing an Akka ActorSystem.
 */
class AkkaModule(name:String,config:Config) extends AbstractModule with ScalaModule {

  override def configure() {
    bind[String].annotatedWith(Names.named("name")).toInstance(name)
    bind[Config].toInstance(config)
    bind[ActorSystem].toProvider[ActorSystemProvider].asEagerSingleton()
  }
}
