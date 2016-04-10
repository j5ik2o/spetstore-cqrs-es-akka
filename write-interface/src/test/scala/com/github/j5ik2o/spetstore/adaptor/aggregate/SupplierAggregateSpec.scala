package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.basic.{Contact, PostalAddress, Pref, ZipCode}
import com.github.j5ik2o.spetstore.domain.item.SupplierAggregateProtocol.Create.{CreateSucceeded, CreateSupplier}
import com.github.j5ik2o.spetstore.domain.item.SupplierAggregateProtocol.Query.{GetStateRequest, GetStateResponse}
import com.github.j5ik2o.spetstore.domain.item.{CategoryId, SupplierId}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.{CommandRequestId, QueryRequestId}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSpecLike}

class SupplierAggregateSpec
  extends TestKit(ActorSystem("SupplierAggregateSpec", ConfigFactory.parseString(
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

  describe("SupplierAggregate") {
    it("生成できること") {
      val id = SupplierId()
      val aggregate = system.actorOf(SupplierAggregate.props(eventBus, id))
      val name = "a"
      val postalAddress = PostalAddress(ZipCode("111", "1111"), Pref.東京都, "港区", "赤坂1丁目", None)
      val contact = Contact("hoge@hoge.com", "hoge@hoge.com")
      aggregate ! CreateSupplier(CommandRequestId(), id, name, postalAddress, contact)
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
      }
    }
  }

}
