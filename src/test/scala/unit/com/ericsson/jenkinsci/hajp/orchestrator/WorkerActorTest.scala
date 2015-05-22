package unit.com.ericsson.jenkinsci.hajp.orchestrator

import java.util

import akka.actor.{Address, ActorSystem}
import akka.testkit.TestActorRef
import com.ericsson.jenkinsci.hajp.messages.credentials.{CredentialsMatchMessage, CredentialsCreateMessage, SecretsAndKeysMatchMessage, SecretsAndKeysMessage}
import com.ericsson.jenkinsci.hajp.orchestrator.orchestrator.WorkerActor
import com.typesafe.config.{ConfigFactory, Config}
import org.scalatest._
import org.scalatest.concurrent._
import org.scalatest.time.{Millis, Seconds, Span}

import org.scalamock.scalatest.MockFactory


import scala.collection.mutable.Stack

class WorkerActorTest extends FlatSpec with Matchers with ActorSystemProvider with ScalaFutures with MockFactory with BeforeAndAfter
with BeforeAndAfterAll{


  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))


  implicit val actorSystem = ActorSystem("testsystem")

  val actorRef = TestActorRef[WorkerActor]
  val actor = actorRef.underlyingActor
  val testStr = "test"
  val testStr2 = "test2"

  val fakeAddr = new Address("akka","testsystem")

  // Creating a dummy secrets and keys msg to set actor variables
  val secretsKeysMsg = new SecretsAndKeysMessage(testStr.getBytes(), testStr.getBytes())
  val credentialsCreateMsg = new CredentialsCreateMessage(testStr2.getBytes(), testStr2.getBytes())


  before {
    // add before each test to be run material here
  }

  after {
    // add after each test to be run material here
    actor.postStop()
  }

  override protected def afterAll(): Unit = {
    actorSystem.shutdown()
  }

  "Important data properties" should "be empty or null" in {
    actor.jenkinsMembers should be (Set())
    actor.activeMasterJenkins should be (null)
    actor.secretsKeysMsg should be (null)
    actor.masterCredentials should be (null)
    actor.proxyConfigPath should be (null)
  }

  "Fixed data propoerties" should "be set to initial values" in {
    actor.jenkinsClusterActorPath should be ("/user/clusterListener")
    actor.jenkinsRoleName should be ("jenkins")
  }

  "Secrets and keys message" should "be set when secretsandkeysmessage is received" in {
    actorRef ! secretsKeysMsg
    actor.secretsKeysMsg should be (secretsKeysMsg)
  }

  "Credentials message" should "be set when Credentials Create Msg is received" in {
    actorRef ! credentialsCreateMsg
    credentialsCreateMsg should not be secretsKeysMsg
    credentialsCreateMsg should be (credentialsCreateMsg)

  }

  "Master credentials" should "be sent when SecretsAndKeysMatchMessage is received" in {
    actor.activeMasterJenkins = fakeAddr
    actorRef ! new SecretsAndKeysMatchMessage
    actor.masterCredentials = null
    actorRef ! new SecretsAndKeysMatchMessage
  }

  "Hot Standby Status" should "be assigned to sender when CredentialsMatchMessage from it" in {
    actor.activeMasterJenkins = fakeAddr
    actorRef ! new CredentialsMatchMessage
  }

  "Post stop" should "return all values to initial " in {
    actor.postStop()
    actor.jenkinsMembers should be (Set())
    actor.activeMasterJenkins should be (null)
    actor.secretsKeysMsg should be (null)
    actor.masterCredentials should be (null)
    actor.proxyConfigPath should be (null)
  }

}

trait ActorSystemProvider {
  implicit def actorSystem: ActorSystem
}
