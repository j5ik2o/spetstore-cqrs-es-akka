package com.github.j5ik2o.spetstore.domain.item

import java.util.UUID

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.item.CategoryAggregateProtocol.Create.CategoryCreated
import com.github.j5ik2o.spetstore.domain.item.CategoryAggregateProtocol.Update.{CategoryUpdateEvent, NameUpdated}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol._
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

object CategoryAggregateProtocol extends EntityProtocol {
  override type Id = CategoryId
  override type CommandRequest = CategoryCommandRequest
  override type CommandResponse = CategoryCommandResponse
  override type Event = CategoryEvent
  override type QueryRequest = CategoryQueryRequest
  override type QueryResponse = CategoryQueryResponse

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
                               id: CommandRequestId,
                               entityId: CategoryId,
                               status: StatusType.Value,
                               name: String,
                               description: Option[String] = None,
                               version: Option[Long]
                             )
      extends CategoryCreateCommandRequest {
      override def toEvent: CategoryCreated =
        CategoryCreated(
          EventId(UUID.randomUUID()),
          entityId,
          status,
          name,
          description,
          version
        )
    }

    case class CreateSucceeded(id: CommandResponseId, commandRequestId: CommandRequestId, entityId: Id)
      extends CommandSucceeded[CategoryId, Category]

    case class CreateFailed(id: CommandResponseId, commandRequestId: CommandRequestId, entityId: Id, throwable: Throwable)
      extends CommandFailed[CategoryId]

    trait CategoryCreateEvent extends CategoryEvent with EntityProtocol.CreateEvent[Id]

    case class CategoryCreated(
                                id: EventId,
                                entityId: CategoryId,
                                status: StatusType.Value,
                                name: String,
                                description: Option[String] = None,
                                version: Option[Long]
                              )
      extends CategoryCreateEvent



  }

  object Update {

    trait CategoryUpdateCommandRequest extends CategoryCommandRequest with EntityProtocol.UpdateCommandRequest[Id] {
      override def toEvent: CategoryUpdateEvent
    }

    case class UpdateName(id: CommandRequestId, entityId: Id, name: String)
      extends CategoryUpdateCommandRequest {
      override def toEvent: NameUpdated = NameUpdated(EventId(UUID.randomUUID()), entityId, name)
    }

    case class UpdateSucceeded(id: CommandResponseId, commandRequestId: CommandRequestId, entityId: Id)
      extends CommandSucceeded[CategoryId, Category]

    case class UpdateFailed(id: CommandResponseId, commandRequestId: CommandRequestId, entityId: Id, throwable: Throwable)
      extends CommandFailed[CategoryId]

    trait CategoryUpdateEvent extends CategoryEvent with EntityProtocol.UpdateEvent[Id]

    case class NameUpdated(id: EventId, entityId: Id, name: String)
      extends CategoryUpdateEvent

  }


}

object Category extends EntityFactory[CategoryId, Category] {

  override type Event = CategoryCreated

  override def createFromEvent: PartialFunction[CategoryCreated, Category] = {
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
  extends BaseEntity[CategoryId] {

  override type This = Category

  override type Event = CategoryUpdateEvent

  def withName(value: String): Category = copy(name = value)

  override def withVersion(version: Long): Entity[CategoryId] =
    copy(version = Some(version))

  override def updateState: StateMachine = {
    case NameUpdated(_, entityId, value) => withName(value)
  }

}

