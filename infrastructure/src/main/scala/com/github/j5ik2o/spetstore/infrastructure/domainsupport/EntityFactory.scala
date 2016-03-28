package com.github.j5ik2o.spetstore.infrastructure.domainsupport

trait EntityFactory[ID <: Identifier[_], E <: EntityWithState[ID]] {

  def createFromEvent: PartialFunction[CreateEvent, E]

}
