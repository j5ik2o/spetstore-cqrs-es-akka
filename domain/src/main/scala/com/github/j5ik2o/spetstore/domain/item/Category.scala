package com.github.j5ik2o.spetstore.domain.item

import java.util.UUID

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.item.CategoryAggregateProtocol.Create.{CategoryCreateEvent, CategoryCreated}
import com.github.j5ik2o.spetstore.domain.item.CategoryAggregateProtocol.Update.{CategoryUpdateEvent, NameUpdated}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.{BaseEntity, Entity, EntityFactory, EntityProtocol}

object CategoryAggregateProtocol extends domainsupport.EntityProtocol {
  type Id = CategoryId
  type CommandRequest = CategoryCommandRequest
  type CommandResponse = CategoryCommandResponse
  type Event = CategoryEvent
  type QueryRequest = CategoryQueryRequest
  type QueryResponse = CategoryQueryResponse

  sealed trait CategoryCommandRequest extends EntityProtocol.CommandRequest[Id]

  sealed trait CategoryCommandResponse extends EntityProtocol.CommandResponse[Id]

  sealed trait CategoryEvent extends EntityProtocol.Event[Id]

  sealed trait CategoryQueryRequest extends EntityProtocol.QueryRequest[Id]

  sealed trait CategoryQueryResponse extends EntityProtocol.QueryResponse[Id]

  object Create {

    trait CategoryCreateCommandRequest extends CategoryCommandRequest with EntityProtocol.CreateCommandRequest[Id] {
      override def toEvent: CategoryCreateEvent
    }

    case class CreateCategory(
                               id: EntityProtocol.CommandRequestId,
                               entityId: CategoryId,
                               status: StatusType.Value,
                               name: String,
                               description: Option[String] = None,
                               version: Option[Long]
                             )
      extends CategoryCreateCommandRequest {
      override def toEvent: CategoryCreated =
        CategoryCreated(
          EntityProtocol.EventId(UUID.randomUUID()),
          entityId,
          status,
          name,
          description,
          version
        )
    }

    case class CreateSucceeded(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id)
      extends EntityProtocol.CommandSucceeded[CategoryId, Category]

    case class CreateFailed(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id, throwable: Throwable)
      extends EntityProtocol.CommandFailed[CategoryId]

    trait CategoryCreateEvent extends CategoryEvent with EntityProtocol.CreateEvent[Id]

    case class CategoryCreated(
                                id: EntityProtocol.EventId,
                                entityId: CategoryId,
                                status: StatusType.Value,
                                name: String,
                                description: Option[String] = None,
                                version: Option[Long]
                              )
      extends CategoryCreateEvent


  }

  object Update {

    sealed trait CategoryUpdateCommandRequest extends CategoryCommandRequest with EntityProtocol.UpdateCommandRequest[Id] {
      override def toEvent: CategoryUpdateEvent
    }

    case class UpdateName(id: EntityProtocol.CommandRequestId, entityId: Id, name: String)
      extends CategoryUpdateCommandRequest {
      override def toEvent: NameUpdated = NameUpdated(EntityProtocol.EventId(UUID.randomUUID()), entityId, name)
    }

    case class UpdateSucceeded(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id)
      extends EntityProtocol.CommandSucceeded[CategoryId, Category]

    case class UpdateFailed(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id, throwable: Throwable)
      extends EntityProtocol.CommandFailed[CategoryId]

    trait CategoryUpdateEvent extends CategoryEvent with EntityProtocol.UpdateEvent[Id]

    case class NameUpdated(id: EntityProtocol.EventId, entityId: Id, name: String)
      extends CategoryUpdateEvent

  }

  object Query {

    case class GetStateRequest(id: EntityProtocol.QueryRequestId, entityId: Id) extends EntityProtocol.GetStateRequest[Id]

    case class GetStateResponse(id: EntityProtocol.QueryResponseId, queryRequestId: EntityProtocol.QueryRequestId, entityId: Id, entity: Option[Category])
      extends EntityProtocol.GetStateResponse[Id, Category]

  }


}

object Category extends EntityFactory[CategoryId, Category, CategoryCreateEvent, CategoryUpdateEvent] {

  override def createFromEvent: PartialFunction[CategoryCreateEvent, Category] = {
    case CategoryCreated(_, categoryId, status, name, description, version) =>
      new Category(categoryId, status, name, description, version)
  }

}

/**
  * カテゴリを表すエンティティ。
  *
  * @param id          識別子
  * @param name        名前
  * @param description 説明
  */
case class Category(
                     id: CategoryId,
                     status: StatusType.Value,
                     name: String,
                     description: Option[String] = None,
                     version: Option[Long]
                   )
  extends BaseEntity[CategoryId, CategoryUpdateEvent] {

  override type This = Category

  def withName(value: String): Category = copy(name = value)

  override def withVersion(version: Long): Entity[CategoryId] =
    copy(version = Some(version))

  override def updateState: StateMachine = {
    case NameUpdated(_, entityId, value) => withName(value)
  }

}

