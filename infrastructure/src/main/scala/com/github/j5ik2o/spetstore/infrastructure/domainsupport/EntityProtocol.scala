package com.github.j5ik2o.spetstore.infrastructure.domainsupport

import java.util.UUID

trait EntityProtocol {
  type Id <: EntityId
  type CommandRequest <: EntityProtocol.CommandRequest[Id]
  type CommandResponse <: EntityProtocol.CommandResponse[Id]
  type Event <: EntityProtocol.Event[Id]
  type QueryRequest <: EntityProtocol.QueryRequest[Id]
  type QueryResponse <: EntityProtocol.QueryResponse[Id]
}

object EntityProtocol {

  case class CommandRequestId(value: UUID = UUID.randomUUID()) extends EntityId

  trait CommandRequest[ID <: EntityId] extends Entity[CommandRequestId] {
    val entityId: ID
  }

  trait CreateCommandRequest[ID <: EntityId] extends CommandRequest[ID] {
    def toEvent: CreateEvent[ID]
  }

  trait UpdateCommandRequest[ID <: EntityId] extends CommandRequest[ID] {
    def toEvent: UpdateEvent[ID]
  }

  case class CommandResponseId(value: UUID = UUID.randomUUID()) extends EntityId

  trait CommandResponse[ID <: EntityId] extends Entity[CommandResponseId] {
    val commandRequestId: CommandRequestId
    val entityId: ID
  }

  trait CommandSucceeded[ID <: EntityId, E <: Entity[ID]] extends CommandResponse[ID]

  trait CommandFailed[ID <: EntityId] extends CommandResponse[ID] {
    val throwable: Throwable
  }

  case class EventId(value: UUID = UUID.randomUUID()) extends EntityId

  trait Event[ID <: EntityId] extends Entity[EventId] {
    val entityId: ID
  }

  trait CreateEvent[ID <: EntityId] extends Event[ID]

  trait UpdateEvent[ID <: EntityId] extends Event[ID]

  case class QueryRequestId(value: UUID = UUID.randomUUID()) extends EntityId

  trait QueryRequest[ID <: EntityId] extends Entity[QueryRequestId] {
    val entityId: ID
  }

  trait GetStateRequest[ID <: EntityId] extends Entity[QueryRequestId] {
    val entityId: ID
  }

  case class QueryResponseId(value: UUID = UUID.randomUUID()) extends EntityId

  trait QueryResponse[ID <: EntityId] extends Entity[QueryResponseId] {
    val queryRequestId: QueryRequestId
    val entityId: ID
  }

  trait GetStateResponse[Id <: EntityId, E <: Entity[Id]] extends Entity[QueryResponseId] {
    val entity: Option[E]
  }

}

