package com.github.j5ik2o.spetstore.domain.item

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

trait ItemEvent extends Event

trait ItemCreateEvent extends ItemEvent with CreateEvent
trait ItemUpdateEvent extends ItemEvent with UpdateEvent

object ItemEvent {

  case class NameUpdated(id: EventId, entityid: ItemId, name: String)
    extends ItemUpdateEvent

}

/**
 * ペットを表すエンティティ。
 *
 * @param id          識別子
 * @param itemTypeId  [[ItemTypeId]]
 * @param name        名前
 * @param description 説明
 * @param price       価格
 */
case class Item(
  id:          ItemId,
  status:      StatusType.Value,
  itemTypeId:  ItemTypeId,
  name:        String,
  description: Option[String]   = None,
  price:       BigDecimal,
  supplierId:  SupplierId,
  version:     Option[Long]
)
    extends BaseEntity[ItemId] {

  override type This = Item

  override type Event = ItemUpdateEvent

  override def withVersion(version: Long): Entity[ItemId] = copy(version = Some(version))

  override def updateState: StateMachine = {
    case ItemEvent.NameUpdated(_, _, value) => copy(name = value)
  }

}

