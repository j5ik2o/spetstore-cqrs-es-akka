package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.persistence.PersistentActor
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

import scala.reflect.ClassTag

abstract class AbstractAggregate[ID <: EntityId, E <: EntityWithState[ID]](eventBus: EventBus, id: ID, createPersistentId: ID => String)
  extends PersistentActor {

  override def persistenceId: String = createPersistentId(id)

  protected val entityFactory: EntityFactory[ID, E#This]

  protected var state: Option[E#This] = None

  protected def initialized = state.isDefined

  def createSucceeded(commandRequest: CommandRequest[ID]): CommandSucceeded[ID, E]

  def createFailed(commandRequest: CommandRequest[ID]): CommandFailed

  def updateSucceeded(commandRequest: CommandRequest[ID]): CommandSucceeded[ID, E]

  def updateFailed(commandRequest: CommandRequest[ID]): CommandFailed

  def getSucceeded(commandRequest: CommandRequest[ID]): CommandSucceeded[ID, E]

  def getFailed(commandRequest: CommandRequest[ID]): CommandFailed

  def createState(event: CreateEvent): Unit = {
    state = Some(entityFactory.createFromEvent(event))
  }

  def updateState(event: UpdateEvent): Unit = {
    state = state.map(_.updateState(event.asInstanceOf[E#This#Event]).asInstanceOf[E#This])
  }

  def createState[A <: CreateCommandRequest[ID] : ClassTag](commandRequest: A): Unit = commandRequest match {
    case commandRequest: A if !initialized =>
      sender() ! createFailed(commandRequest)
    case commandRequest: A if initialized =>
      persist(commandRequest.toEvent) { event =>
        createState(event)
        sender() ! createSucceeded(commandRequest)
      }
  }

  def updateState[A <: UpdateCommandRequest[ID] : ClassTag](commandRequest: A): Unit = commandRequest match {
    case commandRequest: A if !initialized =>
      sender() ! updateFailed(commandRequest)
    case commandRequest: A if initialized =>
      persist(commandRequest.toEvent) { event =>
        updateState(event)
        sender() ! updateSucceeded(commandRequest)
      }
  }

  def getState[A <: GetCommandRequest[ID] : ClassTag](commandRequest: A): Unit = {
    require(commandRequest.entityId == id)
    if (state.isDefined) {
      sender() ! getSucceeded(commandRequest)
    } else {
      sender() ! getFailed(commandRequest)
    }
  }

}
