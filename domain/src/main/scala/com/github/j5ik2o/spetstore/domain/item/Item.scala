package com.github.j5ik2o.spetstore.domain.item

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.item.ItemAggregateProtocol.Update.{ItemUpdateEvent, NameUpdated}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.EventId
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

object ItemAggregateProtocol extends EntityProtocol {
  override type Id = ItemId
  override type CommandRequest = ItemCommandRequest
  override type CommandResponse = ItemCommandResponse
  override type Event = ItemEvent
  override type QueryRequest = ItemQueryRequest
  override type QueryResponse = ItemQueryResponse

  sealed trait ItemCommandRequest extends EntityProtocol.CommandRequest[Id]

  sealed trait ItemCommandResponse extends EntityProtocol.CommandResponse[Id]

  sealed trait ItemEvent extends EntityProtocol.Event[Id]

  sealed trait ItemQueryRequest extends EntityProtocol.QueryRequest[Id]

  sealed trait ItemQueryResponse extends EntityProtocol.QueryResponse[Id]

  object Create {

    trait ItemCreateEvent extends ItemEvent with EntityProtocol.CreateEvent[Id]

  }

  object Update {

    trait ItemUpdateEvent extends ItemEvent with EntityProtocol.UpdateEvent[Id]

    case class NameUpdated(id: EventId, entityId: Id, name: String)
      extends ItemUpdateEvent

  }

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
    case NameUpdated(_, _, value) => copy(name = value)
  }

}

