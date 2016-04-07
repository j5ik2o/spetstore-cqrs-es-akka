package com.github.j5ik2o.spetstore.infrastructure.domainsupport

import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.UpdateEvent

/**
 * DDDのエンティティ責務を表すトレイト。
 *
 * @tparam ID [[Identifier]]
 */
trait Entity[ID <: EntityId] {

  /**
   * 識別子。
   */
  val id: ID

  override def equals(obj: Any): Boolean = this match {
    case that: Entity[_] => id == that.id
    case _               => false
  }

  override def hashCode: Int = 31 * id.##

}

trait EntityWithState[ID <: EntityId] extends Entity[ID] {

  type This <: EntityWithState[ID]

  type Event <: UpdateEvent[ID]

  type StateMachine = PartialFunction[Event, This]

  def updateState: StateMachine

}

trait BaseEntity[ID <: EntityId] extends EntityWithState[ID] {

  val version: Option[Long]

  def withVersion(version: Long): Entity[ID]

}
