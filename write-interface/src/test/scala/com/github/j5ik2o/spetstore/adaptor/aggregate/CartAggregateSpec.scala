package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.customer.CustomerId
import com.github.j5ik2o.spetstore.domain.item.ItemId
import com.github.j5ik2o.spetstore.domain.purchase.CartAggregateProtocol.Create.{ CreateCart, CreateSucceeded }
import com.github.j5ik2o.spetstore.domain.purchase.CartAggregateProtocol.Query.{ GetStateRequest, GetStateResponse }
import com.github.j5ik2o.spetstore.domain.purchase.{ CartId, CartItem, CartItemId }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.{ CommandRequestId, QueryRequestId }
import com.typesafe.config.ConfigFactory
import org.scalatest.{ BeforeAndAfterAll, FunSpecLike }

class CartAggregateSpec
    extends TestKit(ActorSystem("CartAggregateSpecc", ConfigFactory.parseString(
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

  describe("CartAggregate") {
    it("生成できること") {
      val id = CartId()
      val aggregate = system.actorOf(CartAggregate.props(eventBus, id))
      val customerId = CustomerId()
      val cartItems = List(CartItem(
        id = CartItemId(),
        StatusType.Enabled,
        0,
        itemId   = ItemId(),
        quantity = 1,
        inStock  = false,
        Some(1L)
      ))
      aggregate ! CreateCart(CommandRequestId(), id, customerId, cartItems)
      expectMsgType[CreateSucceeded] match {
        case response: CreateSucceeded =>
          assert(response.entityId == id)
      }
      aggregate ! GetStateRequest(QueryRequestId(), id)
      expectMsgType[GetStateResponse] match {
        case response: GetStateResponse =>
          assert(response.entityId == id)
          assert(response.entity.get.id == id)
      }
    }
  }

}
