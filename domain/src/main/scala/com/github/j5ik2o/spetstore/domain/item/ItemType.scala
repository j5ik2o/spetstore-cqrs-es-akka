package com.github.j5ik2o.spetstore.domain.item

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

trait ItemTypeEvent extends Event

trait ItemTypeCreateEvent extends InventoryEvent with CreateEvent
trait ItemTypeUpdateEvent extends InventoryEvent with UpdateEvent

object ItemTypeEvent {

  case class NameUpdated(id: EventId, entityId: ItemTypeId, name: String)
    extends ItemTypeUpdateEvent

  case class DescriptionUpdated(id: EventId, entityId: ItemTypeId, description: Option[String])
    extends ItemTypeUpdateEvent

}

/**
  * 商品の種類を表すエンティティ。
  *
  * @param id          識別子
  * @param categoryId  [[CategoryId]]
  * @param name        名前
  * @param description 説明
  */
case class ItemType
(id: ItemTypeId,
 status: StatusType.Value,
 categoryId: CategoryId,
 name: String,
 description: Option[String] = None,
 version: Option[Long])
  extends BaseEntity[ItemTypeId] {

  override type This = ItemType

  override type Event = ItemTypeUpdateEvent

  override def withVersion(version: Long): Entity[ItemTypeId] =
    copy(version = Some(version))

  override def updateState: StateMachine = {
    case ItemTypeEvent.NameUpdated(_, entityId, value) =>
      require(entityId == id)
      copy(name = value)
    case ItemTypeEvent.DescriptionUpdated(_, entityId, value) =>
      require(entityId == id)
      copy(description = value)
  }

}

