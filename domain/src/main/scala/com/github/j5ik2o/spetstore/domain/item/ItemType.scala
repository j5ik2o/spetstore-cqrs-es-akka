package com.github.j5ik2o.spetstore.domain.item

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.item.ItemTypeAggregateProtocol.Create.{ ItemTypeCreateEvent, ItemTypeCreated }
import com.github.j5ik2o.spetstore.domain.item.ItemTypeAggregateProtocol.Update.{ DescriptionUpdated, ItemTypeUpdateEvent, NameUpdated }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.{ CommandRequestId, CommandResponseId, EventId }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

object ItemTypeAggregateProtocol extends EntityProtocol {
  type Id = ItemTypeId
  type CommandRequest = ItemTypeCommandRequest
  type CommandResponse = ItemTypeCommandResponse
  type Event = ItemTypeEvent
  type QueryRequest = ItemTypeQueryRequest
  type QueryResponse = ItemTypeQueryResponse

  sealed trait ItemTypeCommandRequest extends EntityProtocol.CommandRequest[Id]

  sealed trait ItemTypeCommandResponse extends EntityProtocol.CommandResponse[Id]

  sealed trait ItemTypeEvent extends EntityProtocol.Event[Id]

  sealed trait ItemTypeQueryRequest extends EntityProtocol.QueryRequest[Id]

  sealed trait ItemTypeQueryResponse extends EntityProtocol.QueryResponse[Id]

  object Create {

    trait ItemTypeCreateCommandRequest extends ItemTypeCommandRequest with EntityProtocol.CreateCommandRequest[Id] {
      override def toEvent: ItemTypeCreateEvent
    }

    case class CreateItemType(
      id:          CommandRequestId,
      entityId:    Id,
      categoryId:  CategoryId,
      name:        String,
      description: Option[String]   = None
    ) extends ItemTypeCreateCommandRequest {
      override def toEvent: ItemTypeCreateEvent = ItemTypeCreated(
        EntityProtocol.EventId(),
        entityId,
        categoryId,
        name,
        description
      )
    }

    case class CreateSucceeded(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id)
      extends ItemTypeCommandResponse with EntityProtocol.CommandSucceeded[Id, ItemType]

    case class CreateFailed(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id, throwable: Throwable)
      extends ItemTypeCommandResponse with EntityProtocol.CommandFailed[Id]

    trait ItemTypeCreateEvent extends ItemTypeEvent with EntityProtocol.CreateEvent[Id]

    case class ItemTypeCreated(
      id:          EntityProtocol.EventId,
      entityId:    Id,
      categoryId:  CategoryId,
      name:        String,
      description: Option[String]         = None
    )
        extends ItemTypeCreateEvent

  }

  object Update {

    trait ItemTypeUpdateCommandRequest extends ItemTypeCommandRequest with EntityProtocol.UpdateCommandRequest[Id] {
      override def toEvent: ItemTypeUpdateEvent
    }

    case class UpdateName(
      id:       CommandRequestId,
      entityId: Id,
      name:     String
    )
        extends ItemTypeUpdateCommandRequest {
      override def toEvent: ItemTypeUpdateEvent =
        NameUpdated(EntityProtocol.EventId(), entityId, name)
    }

    case class UpdateSucceeded(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id)
      extends ItemTypeCommandResponse with EntityProtocol.CommandSucceeded[Id, ItemType]

    case class UpdateFailed(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id, throwable: Throwable)
      extends ItemTypeCommandResponse with EntityProtocol.CommandFailed[Id]

    trait ItemTypeUpdateEvent extends ItemTypeEvent with EntityProtocol.UpdateEvent[Id]

    case class NameUpdated(id: EventId, entityId: Id, name: String)
      extends ItemTypeUpdateEvent

    case class DescriptionUpdated(id: EventId, entityId: Id, description: Option[String])
      extends ItemTypeUpdateEvent

  }

  object Query {

    case class GetStateRequest(id: EntityProtocol.QueryRequestId, entityId: Id) extends EntityProtocol.GetStateRequest[Id]

    case class GetStateResponse(id: EntityProtocol.QueryResponseId, queryRequestId: EntityProtocol.QueryRequestId, entityId: Id, entity: Option[ItemType])
      extends EntityProtocol.GetStateResponse[Id, ItemType]

  }
}

object ItemType extends EntityFactory[ItemTypeId, ItemType, ItemTypeCreateEvent, ItemTypeUpdateEvent] {
  override def createFromEvent: PartialFunction[ItemTypeCreateEvent, ItemType] = {
    case ItemTypeCreated(_, id, categoryId, name, description) =>
      ItemType(id, StatusType.Enabled, categoryId, name, description, Some(1L))
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
    extends BaseEntity[ItemTypeId, ItemTypeUpdateEvent] {

  override type This = ItemType

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

