package com.github.j5ik2o.spetstore.infrastructure.domainsupport

import java.util.UUID

case class CommandResponseId(value: UUID = UUID.randomUUID()) extends Identifier[UUID]

trait CommandResponse extends Entity[CommandResponseId] {
  val commandRequestId: CommandRequestId
}

trait CommandSucceeded[ID <: EntityId, E <: Entity[ID]] extends CommandResponse {
  val entity: E
}

trait CommandFailed extends CommandResponse {
  val throwable: Throwable
}

