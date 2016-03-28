package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.{Actor, ActorRef, Props}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.{CommandRequest, CommandResponse, EntityId}

abstract class AbstractMessageBroker[ID <: EntityId, CR <: CommandRequest[ID]] extends Actor {

  def createChildProps(aggregateId: ID): Props

  def actorOf(childName: String, commandRequest: CR): ActorRef =
    context.actorOf(createChildProps(commandRequest.entityId), childName)

  def getChildName(commandRequest: CR): String

  def forwardMessage(commandRequest: CR): Unit = {
    val childName = getChildName(commandRequest)
    context.child(childName).fold(actorOf(childName, commandRequest) forward commandRequest) {
      _ forward commandRequest
    }
  }

}
