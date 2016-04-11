package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.basic._
import com.github.j5ik2o.spetstore.domain.customer.CustomerId
import com.github.j5ik2o.spetstore.domain.item.ItemId
import com.github.j5ik2o.spetstore.domain.model.purchase.OrderStatus
import com.github.j5ik2o.spetstore.domain.purchase.OrderAggregateProtocol.Create.{ CreateOrder, CreateSucceeded }
import com.github.j5ik2o.spetstore.domain.purchase.OrderAggregateProtocol.Query.{ GetStateRequest, GetStateResponse }
import com.github.j5ik2o.spetstore.domain.purchase.{ OrderId, OrderItem, OrderItemId }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.{ CommandRequestId, QueryRequestId }
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import org.scalatest.{ BeforeAndAfterAll, FunSpecLike }

class OrderAggregateSpec
    extends TestKit(ActorSystem("OrderAggregateSpec", ConfigFactory.parseString(
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

  describe("OrderAggregate") {
    it("生成できること") {
      val id = OrderId()
      val aggregate = system.actorOf(OrderAggregate.props(eventBus, id))
      val orderStatus = OrderStatus.Pending
      val orderDate = DateTime.now
      val customerId = CustomerId()
      val customerName = "test"
      val shippingAddress = PostalAddress(ZipCode("111", "1111"), Pref.東京都, "港区", "赤坂1丁目", None)
      val shippingContact = Contact("hoge@hoge.com", "hoge@hoge.com")
      val orderItems = List(OrderItem(
        id       = OrderItemId(),
        status   = StatusType.Enabled,
        no       = 0,
        itemId   = ItemId(),
        quantity = 1,
        version  = Some(1L)
      ))
      aggregate ! CreateOrder(CommandRequestId(), id, orderStatus, orderDate, customerId, customerName, shippingAddress, shippingContact, orderItems)
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
