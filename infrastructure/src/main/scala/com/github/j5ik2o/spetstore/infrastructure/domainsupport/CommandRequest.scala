package com.github.j5ik2o.spetstore.infrastructure.domainsupport

import java.util.UUID

case class CommandRequestId(value: UUID = UUID.randomUUID()) extends Identifier[UUID]

trait CommandRequest[ID <: EntityId] extends Entity[CommandRequestId] {
  val entityId: ID
}

trait CreateCommandRequest[ID <: EntityId] extends CommandRequest[ID] {
  def toEvent: CreateEvent
}

trait UpdateCommandRequest[ID <: EntityId] extends CommandRequest[ID] {
  def toEvent: UpdateEvent
}

trait GetCommandRequest[ID <: EntityId] extends CommandRequest[ID] {

}
