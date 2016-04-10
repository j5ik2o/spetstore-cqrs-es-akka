package com.github.j5ik2o.spetstore.domain.item

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.item.ItemAggregateProtocol.Create.{ ItemCreateEvent, ItemCreated }
import com.github.j5ik2o.spetstore.domain.item.ItemAggregateProtocol.Update.{ ItemUpdateEvent, NameUpdated }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.EventId
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

object ItemAggregateProtocol extends EntityProtocol {
  type Id = ItemId
  type CommandRequest = ItemCommandRequest
  type CommandResponse = ItemCommandResponse
  type Event = ItemEvent
  type QueryRequest = ItemQueryRequest
  type QueryResponse = ItemQueryResponse

  sealed trait ItemCommandRequest extends EntityProtocol.CommandRequest[Id]

  sealed trait ItemCommandResponse extends EntityProtocol.CommandResponse[Id]

  sealed trait ItemEvent extends EntityProtocol.Event[Id]

  sealed trait ItemQueryRequest extends EntityProtocol.QueryRequest[Id]

  sealed trait ItemQueryResponse extends EntityProtocol.QueryResponse[Id]

  object Create {

    trait ItemCreateCommandRequest extends ItemCommandRequest with EntityProtocol.CreateCommandRequest[ItemId] {
      override def toEvent: ItemCreateEvent
    }

    case class CreateItem(
      id:          EntityProtocol.CommandRequestId,
      entityId:    Id,
      itemTypeId:  ItemTypeId,
      name:        String,
      description: Option[String],
      price:       BigDecimal,
      supplierId:  SupplierId
    )
        extends ItemCreateCommandRequest {
      override def toEvent: ItemCreated = ItemCreated(
        EntityProtocol.EventId(),
        entityId,
        itemTypeId,
        name,
        description,
        price,
        supplierId
      )
    }

    case class CreateSucceeded(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id)
      extends ItemCommandResponse with EntityProtocol.CommandSucceeded[Id, Item]

    case class CreateFailed(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id, throwable: Throwable)
      extends EntityProtocol.CommandFailed[Id]

    trait ItemCreateEvent extends ItemEvent with EntityProtocol.CreateEvent[Id]

    case class ItemCreated(
      id:          EntityProtocol.EventId,
      entityId:    Id,
      itemTypeId:  ItemTypeId,
      name:        String,
      description: Option[String],
      price:       BigDecimal,
      supplierId:  SupplierId
    ) extends ItemCreateEvent

  }

  object Update {

    trait ItemUpdateCommandRequest extends ItemCommandRequest with EntityProtocol.UpdateCommandRequest[Id] {
      override def toEvent: ItemUpdateEvent
    }

    case class UpdateName(id: EntityProtocol.CommandRequestId, entityId: Id, name: String) extends ItemUpdateCommandRequest {
      override def toEvent: ItemUpdateEvent = NameUpdated(EntityProtocol.EventId(), entityId, name)
    }

    case class UpdateSucceeded(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id)
      extends ItemCommandResponse with EntityProtocol.CommandSucceeded[Id, Item]

    case class UpdateFailed(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id, throwable: Throwable)
      extends ItemCommandResponse with EntityProtocol.CommandFailed[Id]

    trait ItemUpdateEvent extends ItemEvent with EntityProtocol.UpdateEvent[Id]

    case class NameUpdated(id: EventId, entityId: Id, name: String)
      extends ItemUpdateEvent

  }

  object Query {

    case class GetStateRequest(id: EntityProtocol.QueryRequestId, entityId: Id) extends EntityProtocol.GetStateRequest[Id]

    case class GetStateResponse(id: EntityProtocol.QueryResponseId, queryRequestId: EntityProtocol.QueryRequestId, entityId: Id, entity: Option[Item])
      extends EntityProtocol.GetStateResponse[Id, Item]

  }

}

object Item extends EntityFactory[ItemId, Item, ItemCreateEvent, ItemUpdateEvent] {
  override def createFromEvent: PartialFunction[ItemCreateEvent, Item] = {
    case ItemCreated(_, id, itemTypeId, name, description, price, supplierId) =>
      Item(id, StatusType.Enabled, itemTypeId, name, description, price, supplierId, Some(1L))
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
    extends BaseEntity[ItemId, ItemUpdateEvent] {

  override type This = Item

  override def withVersion(version: Long): Entity[ItemId] = copy(version = Some(version))

  override def updateState: StateMachine = {
    case NameUpdated(_, _, value) => copy(name = value)
  }

}

