package com.github.j5ik2o.spetstore

import java.util.UUID

import akka.actor.{ ActorIdentity, ActorPath, ActorSystem, Identify, Props }
import akka.pattern.ask
import akka.persistence.journal.leveldb.{ SharedLeveldbJournal, SharedLeveldbStore }
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait SharedJournalSupport {
  implicit val timeout = Timeout(10 seconds)

  def startupSharedJournal(startStore: Boolean, path: ActorPath)(implicit system: ActorSystem, ctx: ExecutionContext): Unit = {
    if (startStore)
      system.actorOf(Props[SharedLeveldbStore], "store")
    val actorSelection = system.actorSelection(path)
    val future = actorSelection ? Identify(UUID.randomUUID())
    future.onSuccess {
      case ActorIdentity(_, Some(ref)) =>
        SharedLeveldbJournal.setStore(ref, system)
      case x ⇒
        system.log.error("Shared journal not started at {}", path)
        system.terminate()
    }
    future.onFailure {
      case _ ⇒
        system.log.error("Lookup of shared journal at {} timed out", path)
        system.terminate()
    }
  }
}
