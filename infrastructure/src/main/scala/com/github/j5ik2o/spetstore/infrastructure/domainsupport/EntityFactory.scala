package com.github.j5ik2o.spetstore.infrastructure.domainsupport

import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.CreateEvent

trait EntityFactory[ID <: EntityId, E <: EntityWithState[ID]] {

  type Event <: CreateEvent[ID]

  def createFromEvent: PartialFunction[Event, E]

}
