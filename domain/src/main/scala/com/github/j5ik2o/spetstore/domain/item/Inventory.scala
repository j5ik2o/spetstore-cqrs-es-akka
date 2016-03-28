package com.github.j5ik2o.spetstore.domain.item

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

trait InventoryEvent extends Event

trait InventoryCreateEvent extends InventoryEvent with CreateEvent
trait InventoryUpdateEvent extends InventoryEvent with UpdateEvent

object InventoryEvent {

  case class QuantityUpdated(id: EventId, entityId: InventoryId, quantity: Int)
    extends InventoryUpdateEvent

}

/**
 * 在庫を表すエンティティ。
 *
 * @param id       [[InventoryId]]
 * @param itemId   [[ItemId]]
 * @param quantity 在庫数量
 */
case class Inventory(
  id:       InventoryId,
  status:   StatusType.Value,
  itemId:   ItemId,
  quantity: Int,
  version:  Option[Long]
)
    extends BaseEntity[InventoryId] {

  override type This = Inventory

  override type Event = InventoryUpdateEvent

  override def withVersion(version: Long): Entity[InventoryId] =
    copy(version = Some(version))

  override def updateState: StateMachine = {
    case InventoryEvent.QuantityUpdated(_, entityId, value) =>
      require(entityId == id)
      copy(quantity = value)
  }

}

