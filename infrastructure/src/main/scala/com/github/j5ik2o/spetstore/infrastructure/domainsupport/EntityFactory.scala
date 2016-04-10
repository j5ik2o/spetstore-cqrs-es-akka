package com.github.j5ik2o.spetstore.infrastructure.domainsupport

trait EntityFactory[ID <: EntityId, E <: EntityWithState[ID, EV], CEV <: EntityProtocol.CreateEvent[ID], EV <: EntityProtocol.UpdateEvent[ID]] {

  def createFromEvent: PartialFunction[CEV, E]

}
