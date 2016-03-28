package com.github.j5ik2o.spetstore.infrastructure.domainsupport

import java.util.UUID

case class EventId(value: UUID) extends Identifier[UUID]

trait Event extends Entity[EventId] {

}

trait CreateEvent extends Event

trait UpdateEvent extends Event
