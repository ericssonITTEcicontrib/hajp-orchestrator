package unit.com.ericsson.jenkinsci.hajp.orchestrator

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, ImplicitSender}
import com.ericsson.jenkinsci.hajp.messages.credentials.{SecretsAndKeysMatchMessage, CredentialsCreateMessage, SecretsAndKeysMessage}
import com.ericsson.jenkinsci.hajp.orchestrator.orchestrator.WorkerActor
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, MustMatchers}


class WorkerActorImplicitTest extends TestKit(ActorSystem("testSystemImplicit"))
// Using the ImplicitSender trait will automatically set `testActor` as the sender
with ImplicitSender
with WordSpecLike
with MustMatchers
with BeforeAndAfterAll
{

  override protected def afterAll(): Unit = {
    shutdown(system)
  }

  val testStr = "test"
  val testStr2 = "test2"
  // Creating a dummy secrets and keys msg to set actor variables
  val secretsKeysMsg = new SecretsAndKeysMessage(testStr.getBytes(), testStr.getBytes())
  val credentialsCreateMsg = new CredentialsCreateMessage(testStr2.getBytes(), testStr2.getBytes())

  "A simple actor" must {
    "send back a result" in {
      // Creation of the TestActorRef
      val actorRef = TestActorRef[WorkerActor]
      actorRef.underlyingActor.masterCredentials = credentialsCreateMsg
      actorRef ! new SecretsAndKeysMatchMessage
      // This method assert that if secrets and keys match a credentials message is sent by orchestrator
      expectMsg(credentialsCreateMsg)
    }



  }



}
