package com.ericsson.jenkinsci.hajp.orchestrator.orchestrator

import akka.actor.{Props, ActorSystem, ActorRef, RootActorPath, Address, ActorLogging, Actor}

import java.io.FileWriter

import akka.cluster.{Member, Cluster}
import akka.cluster.ClusterEvent.{InitialStateAsEvents, LeaderChanged, MemberEvent, MemberRemoved, MemberUp, UnreachableMember}
import com.ericsson.jenkinsci.hajp.messages.HajpMessage
import com.ericsson.jenkinsci.hajp.messages.credentials._
import com.ericsson.jenkinsci.hajp.messages.jobs.SendAllJobsMessage
import com.ericsson.jenkinsci.hajp.messages.orchestration.{ActiveMasterAssignmentMessage, HotStandbyAssignmentMessage}

import scala.io.Source
import scala.sys.process._
import scala.concurrent.duration._


/**
 * Implementing cluster role based business logic
 */
class WorkerActor extends Actor with ActorLogging {
  val cluster = Cluster(context.system)
  val jenkinsClusterActorPath = "/user/clusterListener"
  val jenkinsRoleName = "jenkins"
  var jenkinsMembers: Set[Address] = Set()
  var activeMasterJenkins: Address = null
  var secretsKeysMsg: SecretsAndKeysMessage = null
  var masterCredentials: CredentialsCreateMessage = null

  val env = System.getenv()
  var proxyConfigPath: String = null

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    if (env.get("orchestrator_deploy") != null) {
      proxyConfigPath = env.get("orchestrator_deploy") + "/conf/"
    }
    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember],
      classOf[MemberUp], classOf[MemberRemoved], classOf[LeaderChanged])
    //#subscribe
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
    activeMasterJenkins = null
    jenkinsMembers = Set()
    secretsKeysMsg = null
    masterCredentials = null
  }

  def receive = {
    case MemberUp(member) =>
      if (member.getRoles.contains(jenkinsRoleName)) {
        if (activeMasterJenkins == null) {
          assignAMStatus(member)
        } else {
          val hsActorRef = context.actorSelection(RootActorPath(member.address) / "user" / "clusterListener")
          hsActorRef ! secretsKeysMsg
        }
      }
      log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      if (member.getRoles.contains(jenkinsRoleName)) {
        jenkinsMembers = jenkinsMembers - member.address
        log.info(activeMasterJenkins.equals(member.address).toString)
        if (activeMasterJenkins.equals(member.address)) {
          electNewMaster()
        }
      }
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case secretsAndKeysMessage: SecretsAndKeysMessage =>
      log.info("Secrets and keys:" + secretsAndKeysMessage.toString)
      secretsKeysMsg = secretsAndKeysMessage
    case credentialsCreateMessage: CredentialsCreateMessage =>
      log.info("Credentials kept:" + credentialsCreateMessage.toString)
      masterCredentials = credentialsCreateMessage
      if(masterCredentials != null)
        sendMsgToMembers(credentialsCreateMessage)
    case secretMatch: SecretsAndKeysMatchMessage =>
      if(masterCredentials == null){
        assignHSStatus(sender)
      } else {
        sender ! masterCredentials
      }
    case credentialsMatch: CredentialsMatchMessage =>
      assignHSStatus(sender)
    case _: MemberEvent =>

  }

  def assignHSStatus(assignee: ActorRef ): Unit = {
    if (!jenkinsMembers.contains(assignee.path.address)){
      sender() ! new HotStandbyAssignmentMessage()
      jenkinsMembers = jenkinsMembers + assignee.path.address
      val amActorRef = context.actorSelection(RootActorPath(activeMasterJenkins) / "user" / "clusterListener")
      val hsAddress: String = assignee.path.address.toString

      // Delayed send of job messages
      val system = context.system
      import system.dispatcher
      system.scheduler.scheduleOnce(5 seconds, new Runnable() {
        def run() {
          amActorRef ! new SendAllJobsMessage(activeMasterJenkins.toString, hsAddress)
        }
      })
    }
  }

  def assignAMStatus(assignee: Member): Unit = {
    activeMasterJenkins = assignee.address
    updateReloadProxy()
    val actorRef = context.actorSelection(RootActorPath(assignee.address) / "user" / "clusterListener")
    actorRef ! new ActiveMasterAssignmentMessage
    jenkinsMembers = jenkinsMembers + activeMasterJenkins
  }

  def sendMsgToMembers(hajpMsg: HajpMessage): Unit = {
    log.info(jenkinsMembers.size.toString)
    for(member <- jenkinsMembers){
      context.actorSelection(RootActorPath(member) / "user" / "clusterListener") ! hajpMsg
    }
  }

  def electNewMaster(): Unit = {
    secretsKeysMsg = null
    masterCredentials = null
    jenkinsMembers.isEmpty match {
      case true => activeMasterJenkins = null
      case false => activeMasterJenkins = jenkinsMembers.head
        updateReloadProxy()
        context.actorSelection(RootActorPath(activeMasterJenkins) / "user" / "clusterListener") ! new ActiveMasterAssignmentMessage()
    }
  }

  def updateReloadProxy(): Unit = {
    log.debug(proxyConfigPath)
    if (proxyConfigPath != null && updateAMProxy(activeMasterJenkins) && reloadHaProxy()) {
      log.info("HAJP Proxy successfully reloaded")
    }
    else log.warning("HAJP Proxy can not be reloaded")
  }

  def updateAMProxy(activeMasterAddress: Address): Boolean = {
    val pw = new FileWriter(proxyConfigPath + "haproxy_tmp.cfg")
    val haProxyNewStr = Source
      .fromFile(proxyConfigPath + "haproxy.cfg")
      .getLines
      .map { line =>
      line.replaceAll("webserver1 .*", "webserver1 " + activeMasterAddress.host.get + ":8080")
    }
    for (str <- haProxyNewStr) {
      pw.write(str)
      pw.write("\n")
    }
    pw.close()

    val result = "mv conf/haproxy_tmp.cfg conf/haproxy.cfg".!
    result.equals(0)
  }

  def reloadHaProxy(): Boolean = {
    val lines = scala.io.Source.fromFile(proxyConfigPath + "haproxy.pid").mkString
    val cmdStr = "haproxy -f conf/haproxy.cfg -p conf/haproxy.pid -D -sf " + lines
    val result = cmdStr.!
    result.equals(0)
  }
}

/**
 * Bootup orchestrator backend worker actor
 */
object OrchestratorBackend {
  def startOn(system: ActorSystem) {
    system.actorOf(Props[WorkerActor], name = "orchestratorBackend")
  }
}
