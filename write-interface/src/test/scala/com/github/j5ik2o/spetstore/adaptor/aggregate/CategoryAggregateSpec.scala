package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.item.CategoryAggregateProtocol.Create.{CreateCategory, CreateSucceeded}
import com.github.j5ik2o.spetstore.domain.item.CategoryId
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.CommandRequestId
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSpecLike}

class CategoryAggregateSpec extends TestKit(ActorSystem("CategoryAggregateSpec", ConfigFactory.parseString(
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

  describe("CategoryAggregateSpec") {
    it("生成できること") {
      val id = CategoryId()
      val aggregate = system.actorOf(CategoryAggregate.props(eventBus, id))
      aggregate ! CreateCategory(CommandRequestId(), id, StatusType.Enabled, "", Some(""), Some(1L))
      expectMsgType[CreateSucceeded] match {
        case response: CreateSucceeded =>
          assert(response.entityId == id)
      }
    }
    it("異なるIDで生成できないこと") {
      val id = CategoryId()
      val wrongId = CategoryId()
      val aggregate = TestActorRef[CategoryAggregate](CategoryAggregate.props(eventBus, id))
      val actor = aggregate.underlyingActor
      intercept[IllegalArgumentException] {
        actor.receive(CreateCategory(CommandRequestId(), wrongId, StatusType.Enabled, "", Some(""), Some(1L)))
      }
    }
  }

}
