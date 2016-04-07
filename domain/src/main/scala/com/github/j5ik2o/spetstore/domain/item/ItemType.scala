package com.github.j5ik2o.spetstore.domain.item

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.item.InventoryAggregateProtocol.InventoryEvent
import com.github.j5ik2o.spetstore.domain.item.ItemTypeAggregateProtocol.Update.{DescriptionUpdated, ItemTypeUpdateEvent, NameUpdated}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.EventId
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

object ItemTypeAggregateProtocol extends EntityProtocol {
  override type Id = ItemTypeId
  override type CommandRequest = ItemTypeCommandRequest
  override type CommandResponse = ItemTypeCommandResponse
  override type Event = ItemTypeEvent
  override type QueryRequest = ItemTypeQueryRequest
  override type QueryResponse = ItemTypeQueryResponse

  sealed trait ItemTypeCommandRequest extends EntityProtocol.CommandRequest[Id]

  sealed trait ItemTypeCommandResponse extends EntityProtocol.CommandResponse[Id]

  sealed trait ItemTypeEvent extends EntityProtocol.Event[Id]

  sealed trait ItemTypeQueryRequest extends EntityProtocol.QueryRequest[Id]

  sealed trait ItemTypeQueryResponse extends EntityProtocol.QueryResponse[Id]

  object Create {

    trait ItemTypeCreateEvent extends ItemTypeEvent with EntityProtocol.CreateEvent[Id]

  }
  object Update {

    trait ItemTypeUpdateEvent extends ItemTypeEvent with EntityProtocol.UpdateEvent[Id]

    case class NameUpdated(id: EventId, entityId: ItemTypeId, name: String)
      extends ItemTypeUpdateEvent

    case class DescriptionUpdated(id: EventId, entityId: ItemTypeId, description: Option[String])
      extends ItemTypeUpdateEvent

  }

}


/**
 * 商品の種類を表すエンティティ。
 *
 * @param id          識別子
 * @param categoryId  [[CategoryId]]
 * @param name        名前
 * @param description 説明
 */
case class ItemType(
  id:          ItemTypeId,
  status:      StatusType.Value,
  categoryId:  CategoryId,
  name:        String,
  description: Option[String]   = None,
  version:     Option[Long]
)
    extends BaseEntity[ItemTypeId] {

  override type This = ItemType

  override type Event = ItemTypeUpdateEvent

  override def withVersion(version: Long): Entity[ItemTypeId] =
    copy(version = Some(version))

  override def updateState: StateMachine = {
    case NameUpdated(_, entityId, value) =>
      require(entityId == id)
      copy(name = value)
    case DescriptionUpdated(_, entityId, value) =>
      require(entityId == id)
      copy(description = value)
  }

}

