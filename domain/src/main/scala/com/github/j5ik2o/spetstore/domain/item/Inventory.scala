package com.github.j5ik2o.spetstore.domain.item

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.item.InventoryAggregateProtocol.Update.{InventoryUpdateEvent, QuantityUpdated}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.EventId
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

object InventoryAggregateProtocol extends EntityProtocol {

  override type Id = InventoryId
  override type CommandRequest = InventoryCommandRequest
  override type CommandResponse = InventoryCommandResponse
  override type Event = InventoryEvent
  override type QueryRequest = InventoryQueryRequest
  override type QueryResponse = InventoryQueryResponse

  sealed trait InventoryCommandRequest extends EntityProtocol.CommandRequest[Id]

  sealed trait InventoryCommandResponse extends EntityProtocol.CommandResponse[Id]

  sealed trait InventoryEvent extends EntityProtocol.Event[Id]

  sealed trait InventoryQueryRequest extends EntityProtocol.QueryRequest[Id]

  sealed trait InventoryQueryResponse extends EntityProtocol.QueryResponse[Id]


  object Create {

    trait InventoryCreateEvent extends InventoryEvent with EntityProtocol.CreateEvent[Id]

  }

  object Update {

    trait InventoryUpdateEvent extends InventoryEvent with EntityProtocol.UpdateEvent[Id]

    case class QuantityUpdated(id: EventId, entityId: InventoryId, quantity: Int)
      extends InventoryUpdateEvent

  }

}

/**
  * 在庫を表すエンティティ。
  *
  * @param id       [[InventoryId]]
  * @param itemId   [[ItemId]]
  * @param quantity 在庫数量
  */
case class Inventory(
                      id: InventoryId,
                      status: StatusType.Value,
                      itemId: ItemId,
                      quantity: Int,
                      version: Option[Long]
                    )
  extends BaseEntity[InventoryId, InventoryUpdateEvent] {

  override type This = Inventory

  override def withVersion(version: Long): Entity[InventoryId] =
    copy(version = Some(version))

  override def updateState: StateMachine = {
    case QuantityUpdated(_, entityId, value) =>
      require(entityId == id)
      copy(quantity = value)
  }

}

