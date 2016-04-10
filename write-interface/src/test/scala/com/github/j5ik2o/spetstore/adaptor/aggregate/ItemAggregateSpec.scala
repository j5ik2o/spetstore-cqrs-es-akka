package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestActorRef, TestKit }
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.item.ItemAggregateProtocol.Create.{ CreateItem, CreateSucceeded }
import com.github.j5ik2o.spetstore.domain.item.ItemAggregateProtocol.Query.{ GetStateRequest, GetStateResponse }
import com.github.j5ik2o.spetstore.domain.item.ItemAggregateProtocol.Update.{ UpdateName, UpdateSucceeded }
import com.github.j5ik2o.spetstore.domain.item.{ ItemId, ItemTypeId, SupplierId }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.{ CommandRequestId, QueryRequestId }
import com.typesafe.config.ConfigFactory
import org.scalatest.{ BeforeAndAfterAll, FunSpecLike }

class ItemAggregateSpec
    extends TestKit(ActorSystem("ItemAggregateSpec", ConfigFactory.parseString(
      """
      |akka {
      |  loglevel = DEBUG
      |  persistence.journal.plugin = "akka.persistence.journal.inmem"
      |}
    """.stripMargin
    ))) with ImplicitSender with FunSpecLike with BeforeAndAfterAll {

  val eventBus = EventBus.ofLocal(system)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  describe("ItemAggregate") {
    it("生成できること") {
      val id = ItemId()
      val aggregate = system.actorOf(ItemAggregate.props(eventBus, id))
      val itemTypeId = ItemTypeId()
      val name = "a"
      val description: Option[String] = Some("")
      val price = BigDecimal(0)
      val supplierId = SupplierId()
      aggregate ! CreateItem(CommandRequestId(), id, itemTypeId, name, description, price, supplierId)
      expectMsgType[CreateSucceeded] match {
        case response: CreateSucceeded =>
          assert(response.entityId == id)
      }
      aggregate ! GetStateRequest(QueryRequestId(), id)
      expectMsgType[GetStateResponse] match {
        case response: GetStateResponse =>
          assert(response.entityId == id)
          assert(response.entity.get.id == id)
          assert(response.entity.get.name == name)
          assert(response.entity.get.description == description)
      }
    }
    it("異なるIDで生成できないこと") {
      val id = ItemId()
      val wrongId = ItemId()
      val itemTypeId = ItemTypeId()
      val name = "a"
      val description: Option[String] = Some("")
      val price = BigDecimal(0)
      val supplierId = SupplierId()
      val aggregate = TestActorRef[ItemAggregate](ItemAggregate.props(eventBus, id))
      val actor = aggregate.underlyingActor
      intercept[IllegalArgumentException] {
        actor.receive(CreateItem(CommandRequestId(), wrongId, itemTypeId, name, description, price, supplierId))
      }
    }
    it("更新できること") {
      val id = ItemId()
      val itemTypeId = ItemTypeId()
      val name = "a"
      val description: Option[String] = Some("")
      val price = BigDecimal(0)
      val supplierId = SupplierId()
      val name2 = "b"
      val aggregate = system.actorOf(ItemAggregate.props(eventBus, id))
      aggregate ! CreateItem(CommandRequestId(), id, itemTypeId, name, description, price, supplierId)
      receiveN(1)
      aggregate ! GetStateRequest(QueryRequestId(), id)
      expectMsgType[GetStateResponse] match {
        case response: GetStateResponse =>
          assert(response.entityId == id)
          assert(response.entity.get.id == id)
          assert(response.entity.get.name == name)
          assert(response.entity.get.description == description)
      }
      aggregate ! UpdateName(CommandRequestId(), id, name2)
      expectMsgType[UpdateSucceeded] match {
        case response: UpdateSucceeded =>
          assert(response.entityId == id)
      }
      aggregate ! GetStateRequest(QueryRequestId(), id)
      expectMsgType[GetStateResponse] match {
        case response: GetStateResponse =>
          assert(response.entityId == id)
          assert(response.entity.get.id == id)
          assert(response.entity.get.name == name2)
      }
    }
  }

}
