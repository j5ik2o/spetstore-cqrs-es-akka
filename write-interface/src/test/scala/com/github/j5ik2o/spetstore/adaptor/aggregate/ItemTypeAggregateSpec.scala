package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.item.ItemTypeAggregateProtocol.Create.{ CreateItemType, CreateSucceeded }
import com.github.j5ik2o.spetstore.domain.item.ItemTypeAggregateProtocol.Query.{ GetStateRequest, GetStateResponse }
import com.github.j5ik2o.spetstore.domain.item.{ CategoryId, ItemTypeId }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.{ CommandRequestId, QueryRequestId }
import com.typesafe.config.ConfigFactory
import org.scalatest.{ BeforeAndAfterAll, FunSpecLike }

class ItemTypeAggregateSpec
    extends TestKit(ActorSystem("ItemTypeAggregateSpec", ConfigFactory.parseString(
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

  describe("ItemTypeAggregate") {
    it("生成できること") {
      val id = ItemTypeId()
      val aggregate = system.actorOf(ItemTypeAggregate.props(eventBus, id))
      val name = "a"
      val description: Option[String] = Some("")
      val categoryId = CategoryId()
      val price = BigDecimal(0)
      aggregate ! CreateItemType(CommandRequestId(), id, categoryId, name, description)
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
  }

}
