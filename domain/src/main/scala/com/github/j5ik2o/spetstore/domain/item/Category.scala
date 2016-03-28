package com.github.j5ik2o.spetstore.domain.item

import java.util.UUID

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

sealed trait CategoryCommandRequest extends CommandRequest[CategoryId]

trait CategoryCreateCommandRequest extends CategoryCommandRequest with CreateCommandRequest[CategoryId] {
  override def toEvent: CategoryCreateEvent
}

trait CategoryUpdateCommandRequest extends CategoryCommandRequest with UpdateCommandRequest[CategoryId] {
  override def toEvent: CategoryUpdateEvent
}

trait CategoryGetCommandRequest extends CategoryCommandRequest with GetCommandRequest[CategoryId]

object CategoryCommandRequest {

  case class CreateCategory(
    id:          CommandRequestId,
    entityId:    CategoryId,
    status:      StatusType.Value,
    name:        String,
    description: Option[String]   = None,
    version:     Option[Long]
  )
      extends CategoryCreateCommandRequest {
    override def toEvent: CategoryEvent.CategoryCreated =
      CategoryEvent.CategoryCreated(
        EventId(UUID.randomUUID()),
        entityId,
        status,
        name,
        description,
        version
      )
  }

  case class UpdateName(id: CommandRequestId, entityId: CategoryId, name: String)
      extends CategoryUpdateCommandRequest {
    override def toEvent: CategoryEvent.NameUpdated = CategoryEvent.NameUpdated(EventId(UUID.randomUUID()), name)
  }

  case class GetCategory(id: CommandRequestId, entityId: CategoryId) extends CategoryGetCommandRequest

}

object CategoryCommandResponse {

  case class CreateSucceeded(id: CommandResponseId, commandRequestId: CommandRequestId, entity: Category)
    extends CommandSucceeded[CategoryId, Category]

  case class CreateFailed(id: CommandResponseId, commandRequestId: CommandRequestId, throwable: Throwable)
    extends CommandFailed

  case class UpdateSucceeded(id: CommandResponseId, commandRequestId: CommandRequestId, entity: Category)
    extends CommandSucceeded[CategoryId, Category]

  case class UpdateFailed(id: CommandResponseId, commandRequestId: CommandRequestId, throwable: Throwable)
    extends CommandFailed

  case class GetSucceeded(id: CommandResponseId, commandRequestId: CommandRequestId, entity: Category)
    extends CommandSucceeded[CategoryId, Category]

  case class GetFailed(id: CommandResponseId, commandRequestId: CommandRequestId, throwable: Throwable)
    extends CommandFailed

}

sealed trait CategoryEvent extends Event

trait CategoryCreateEvent extends CategoryEvent with CreateEvent

trait CategoryUpdateEvent extends CategoryEvent with UpdateEvent

object CategoryEvent {

  case class CategoryCreated(
    id:          EventId,
    categoryId:  CategoryId,
    status:      StatusType.Value,
    name:        String,
    description: Option[String]   = None,
    version:     Option[Long]
  )
      extends CategoryCreateEvent

  case class NameUpdated(id: EventId, name: String)
    extends CategoryUpdateEvent

}

object Category extends EntityFactory[CategoryId, Category] {
  override def createFromEvent: PartialFunction[CreateEvent, Category] = {
    case CategoryEvent.CategoryCreated(_, categoryId, status, name, description, version) =>
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
  id:          CategoryId,
  status:      StatusType.Value,
  name:        String,
  description: Option[String]   = None,
  version:     Option[Long]
)
    extends BaseEntity[CategoryId] {

  override type This = Category

  override type Event = CategoryUpdateEvent

  def withName(value: String): Category = copy(name = value)

  override def withVersion(version: Long): Entity[CategoryId] =
    copy(version = Some(version))

  override def updateState: StateMachine = {
    case CategoryEvent.NameUpdated(_, value) => withName(value)
  }

}

