package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestActorRef, TestKit }
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.basic._
import com.github.j5ik2o.spetstore.domain.customer.CustomerAggregateProtocol.Create.{ CreateCustomer, CreateSucceeded }
import com.github.j5ik2o.spetstore.domain.customer.CustomerAggregateProtocol.Query.{ GetStateRequest, GetStateResponse }
import com.github.j5ik2o.spetstore.domain.customer.CustomerAggregateProtocol.Update.{ UpdateName, UpdateSucceeded }
import com.github.j5ik2o.spetstore.domain.customer.{ CustomerConfig, CustomerId, CustomerProfile }
import com.github.j5ik2o.spetstore.domain.item.CategoryId
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.{ CommandRequestId, QueryRequestId }
import com.typesafe.config.ConfigFactory
import org.scalatest.{ BeforeAndAfterAll, FunSpecLike }

class CustomerAggregateSpec extends TestKit(ActorSystem("CustomerAggregateSpec", ConfigFactory.parseString(
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

  def createProfile(): CustomerProfile = {
    val name = "test"
    val sexType = SexType.Male
    val postalAddress = PostalAddress(ZipCode("111", "1111"), Pref.東京都, "港区", "赤坂1丁目", None)
    val contact = Contact("hoge@hoge.com", "hoge@hoge.com")
    CustomerProfile(name, sexType, postalAddress, contact)
  }

  def createConfig(): CustomerConfig = {
    val loginName = "test"
    val password = "test"
    val favoriteCategoryId: Option[CategoryId] = Some(CategoryId())
    CustomerConfig(loginName, password, favoriteCategoryId)
  }

  describe("CustomerAggregate") {
    it("生成できること") {
      val id = CustomerId()
      val aggregate = system.actorOf(CustomerAggregate.props(eventBus, id))
      val profile = createProfile()
      val config = createConfig()
      aggregate ! CreateCustomer(CommandRequestId(), id, StatusType.Enabled, profile, config, Some(1L))
      expectMsgType[CreateSucceeded] match {
        case response: CreateSucceeded =>
          assert(response.entityId == id)
      }
      aggregate ! GetStateRequest(QueryRequestId(), id)
      expectMsgType[GetStateResponse] match {
        case response: GetStateResponse =>
          assert(response.entityId == id)
          assert(response.entity.get.id == id)
          assert(response.entity.get.profile.name == profile.name)
      }
    }
    it("異なるIDで生成できないこと") {
      val id = CustomerId()
      val wrongId = CustomerId()
      val aggregate = TestActorRef[CustomerAggregate](CustomerAggregate.props(eventBus, id))
      val actor = aggregate.underlyingActor
      val profile = createProfile()
      val config = createConfig()
      intercept[IllegalArgumentException] {
        actor.receive(CreateCustomer(CommandRequestId(), wrongId, StatusType.Enabled, profile, config, Some(1L)))
      }
    }
    it("更新できること") {
      val id = CustomerId()
      val name2 = "b"
      val aggregate = system.actorOf(CustomerAggregate.props(eventBus, id))
      val profile = createProfile()
      val config = createConfig()
      aggregate ! CreateCustomer(CommandRequestId(), id, StatusType.Enabled, profile, config, Some(1L))
      receiveN(1)
      aggregate ! GetStateRequest(QueryRequestId(), id)
      expectMsgType[GetStateResponse] match {
        case response: GetStateResponse =>
          assert(response.entityId == id)
          assert(response.entity.get.id == id)
          assert(response.entity.get.profile.name == profile.name)
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
          assert(response.entity.get.profile.name == name2)
      }
    }
  }

}
