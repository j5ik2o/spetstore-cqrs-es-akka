package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.persistence.PersistentActor
import com.github.j5ik2o.spetstore.adaptor.eventbus.{EventBus, EventWithSender}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.{CreateEvent, UpdateEvent}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

import scala.concurrent.duration._
import scala.reflect.ClassTag

abstract class AbstractAggregate[ID <: EntityId, E <: EntityWithState[ID, UEV], CEV <: CreateEvent[ID], UEV <: UpdateEvent[ID]](eventBus: EventBus, id: ID, createPersistentId: ID => String)
  extends PersistentActor {

  override def persistenceId: String = createPersistentId(id)

  protected val entityFactory: EntityFactory[ID, E#This, CEV, UEV]

  protected var state: Option[E#This] = None

  protected def initialized = state.isDefined

  context.setReceiveTimeout(1 seconds)

  def applyCreateEvent(event: CEV): Unit = {
    state = Some(entityFactory.createFromEvent(event))
  }

  def applyUpdateEvent(event: UEV): Unit = {
    state = state.map(_.updateState(event).asInstanceOf[E#This])
  }

  def createSucceeded[C <: EntityProtocol.CommandRequest[ID] : ClassTag](commandRequest: C): EntityProtocol.CommandSucceeded[ID, E]

  def createFailed[C <: EntityProtocol.CommandRequest[ID] : ClassTag](commandRequest: C): EntityProtocol.CommandFailed[ID]

  def updateSucceeded[C <: EntityProtocol.CommandRequest[ID] : ClassTag](commandRequest: C): EntityProtocol.CommandSucceeded[ID, E]

  def updateFailed[C <: EntityProtocol.CommandRequest[ID] : ClassTag](commandRequest: C): EntityProtocol.CommandFailed[ID]

  def getSucceeded[Q <: EntityProtocol.GetStateRequest[ID] : ClassTag](queryRequest: Q): EntityProtocol.GetStateResponse[ID, E]

  def createState[C <: EntityProtocol.CreateCommandRequest[ID] : ClassTag](commandRequest: C): Unit = commandRequest match {
    case commandRequest: C if initialized =>
      require(commandRequest.entityId == id)
      sender() ! createFailed(commandRequest)
    case commandRequest: C if !initialized =>
      require(commandRequest.entityId == id)
      persist(commandRequest.toEvent) { event =>
        applyCreateEvent(event.asInstanceOf[CEV])
        sender() ! createSucceeded(commandRequest)
        eventBus.publish(EventWithSender(event, sender()))
      }
  }

  def updateState[C <: EntityProtocol.UpdateCommandRequest[ID] : ClassTag](commandRequest: C): Unit = commandRequest match {
    case commandRequest: C if !initialized =>
      require(commandRequest.entityId == id)
      sender() ! updateFailed(commandRequest)
    case commandRequest: C if initialized =>
      require(commandRequest.entityId == id)
      persist(commandRequest.toEvent) { event =>
        applyUpdateEvent(event.asInstanceOf[UEV])
        sender() ! updateSucceeded(commandRequest)
        eventBus.publish(EventWithSender(event, sender()))
      }
  }

  def getState[Q <: EntityProtocol.GetStateRequest[ID] : ClassTag](commandRequest: Q): Unit = {
    require(commandRequest.entityId == id)
    sender() ! getSucceeded(commandRequest)
  }

}
