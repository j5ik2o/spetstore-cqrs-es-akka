package com.github.j5ik2o.spetstore.adaptor.aggregate

import java.util.{ Currency, Locale, UUID }

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props }
import akka.persistence.PersistentActor
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import com.github.j5ik2o.spetstore.adaptor.aggregate.BankAccount.Commands.{ Decrease, GetBalanceRequest, Increase }
import com.github.j5ik2o.spetstore.adaptor.aggregate.BankAccount.Context
import com.github.j5ik2o.spetstore.adaptor.aggregate.BankAccount.Events.{ Decreased, GetBalanceResponse, Increased }
import com.github.j5ik2o.spetstore.adaptor.aggregate.Base.Commands.CommandSucceeded
import com.github.j5ik2o.spetstore.adaptor.aggregate.Base.{ Data, Empty, Started, Stopped }
import com.github.j5ik2o.spetstore.adaptor.aggregate.TransferDomainService.Commands.Transfer
import com.github.j5ik2o.spetstore.adaptor.aggregate.TransferDomainService.Events.{ Transferred, Transferring }
import com.github.j5ik2o.spetstore.adaptor.aggregate.TransferDomainService.{ FromDecreased, TransferData }
import com.typesafe.config.ConfigFactory

import scala.reflect._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

object Base {

  sealed trait State extends FSMState

  case object Stopped extends State {
    override def identifier: String = "Stopped"
  }

  case object Started extends State {
    override def identifier: String = "Started"
  }

  trait Data

  case object Empty extends Data

  object Commands {

    case class CommandSucceeded(id: UUID, requestId: UUID) extends Events.Event

    case class CommandFailed(id: UUID, requestId: UUID, ex: Throwable)

  }

  object Events {

    trait Event

  }

}

object Money {
  val Zero = Money(Currency.getInstance(Locale.getDefault), 0)
}

case class Money(currency: Currency, amount: Long) {
  def plus(money: Money): Money = {
    require(currency == money.currency)
    copy(amount = amount + money.amount)
  }

  def +(money: Money): Money = plus(money)

  def minus(money: Money): Money = {
    require(currency == money.currency)
    copy(amount = amount - money.amount)
  }

  def -(money: Money): Money = minus(money)
}

object BankAccount {

  object Commands {

    case class GetBalanceRequest(id: UUID)

    case class Decrease(id: UUID, money: Money)

    case class Increase(id: UUID, money: Money)

  }

  object Events {

    case class GetBalanceResponse(id: UUID, balance: Money)

    case class Decreased(id: UUID, money: Money) // extends Base.Events.Event

    case class Increased(id: UUID, money: Money) // extends Base.Events.Event

  }

  def props(id: UUID): Props = Props(new BankAccount(id))

  case class Context(id: UUID, balance: Money) extends Data {
    def add(money: Money): Context = copy(balance = balance + money)

    def remove(money: Money): Context = copy(balance = balance - money)
  }

}

class BankAccount(id: UUID) extends PersistentActor with ActorLogging {

  override def persistenceId: String = id.toString

  var state = Context(UUID.randomUUID(), Money.Zero)

  override def receiveRecover: Receive = {
    case event: Increased =>
      state = state.add(event.money)
    case event: Decreased =>
      state = state.remove(event.money)
  }

  override def receiveCommand: Receive = {
    case GetBalanceRequest(_) =>
      sender() ! GetBalanceResponse(UUID.randomUUID(), state.balance)
    case Increase(_, money) =>
      persist(Increased(UUID.randomUUID(), money)) { increased =>
        state = state.add(increased.money)
        log.debug("Increase = " + state.toString)
        sender() ! CommandSucceeded(UUID.randomUUID(), increased.id)
        context.system.eventStream.publish(increased)
      }
    case Decrease(_, money) =>
      persist(Decreased(UUID.randomUUID(), money)) { decreased =>
        state = state.remove(decreased.money)
        log.debug("Decrease = " + state.toString)
        sender() ! CommandSucceeded(UUID.randomUUID(), decreased.id)
        context.system.eventStream.publish(decreased)
      }
  }
}

object TransferDomainService {

  case object FromDecreased extends Base.State {
    override def identifier: String = "FromDecreased"
  }

  case object ToIncreased extends Base.State {
    override def identifier: String = "ToIncreased"
  }

  def props(id: UUID): Props = Props(new TransferDomainService(id))

  case class TransferData(id: UUID, money: Money, from: ActorRef, to: ActorRef) extends Base.Data

  object Commands {

    case class Transfer(id: UUID, money: Money, from: ActorRef, to: ActorRef)

  }

  object Events {

    case class Transferring(id: UUID, money: Money, from: ActorRef, to: ActorRef) extends Base.Events.Event

    case class Transferred(id: UUID, money: Money, from: ActorRef, to: ActorRef) extends Base.Events.Event

  }

}

class TransferDomainService(id: UUID)
    extends PersistentFSM[Base.State, Data, Base.Events.Event] {

  override def persistenceId: String = id.toString

  override def domainEventClassTag: ClassTag[Base.Events.Event] = classTag[Base.Events.Event]

  override def applyEvent(domainEvent: Base.Events.Event, currentData: Data): Data = domainEvent match {
    case Transferred(id, money, from, to) =>
      TransferData(id, money, from, to)
    case Transferring(id, money, from, to) =>
      TransferData(id, money, from, to)
  }

  startWith(Stopped, Empty)

  when(Stopped) {
    case Event(Transfer(requestId, money, from, to), _) =>
      context.system.eventStream.subscribe(self, classOf[Decreased])
      val ev = Transferring(UUID.randomUUID(), money, from, to)
      goto(Started) applying ev andThen {
        case d: TransferData =>
          from ! Decrease(UUID.randomUUID(), money)
          sender() ! CommandSucceeded(UUID.randomUUID(), requestId)
          context.system.eventStream.publish(ev)
      }
  }

  when(Started) {
    case Event(Decreased(_, _), _) =>
      goto(FromDecreased) andThen {
        case d @ TransferData(_, money, from, to) =>
          context.system.eventStream.unsubscribe(self, classOf[Decreased])
          context.system.eventStream.subscribe(self, classOf[Increased])
          to ! Increase(UUID.randomUUID(), money)
      }
    case ev @ Event(CommandSucceeded(_, _), _) =>
      stay
  }

  when(FromDecreased) {
    case Event(Increased(_, _), TransferData(_, money, from, to)) =>
      goto(Stopped) applying Transferred(UUID.randomUUID(), money, from, to) andThen {
        case d: TransferData =>
          context.system.eventStream.unsubscribe(self, classOf[Increased])
          context.system.eventStream.publish(Transferred(UUID.randomUUID(), money, from, to))
      }
    case ev @ Event(CommandSucceeded(_, _), _) =>
      stay
  }

  initialize()

}

object Main extends App {
  val system = ActorSystem("PM", ConfigFactory.parseString(
    """
      |akka {
      |  loglevel = DEBUG
      |  persistence.journal.plugin = "akka.persistence.journal.inmem"
      |}
    """.stripMargin
  ))
  implicit val timeout = Timeout(5 seconds)
  import system.dispatcher

  val JPY = Currency.getInstance("JPY")

  val to = system.actorOf(Props(new BankAccount(UUID.randomUUID())))
  to ! Increase(UUID.randomUUID(), Money(JPY, 10))

  val from = system.actorOf(Props(new BankAccount(UUID.randomUUID())))
  from ! Increase(UUID.randomUUID(), Money(JPY, 10))

  val actor = system.actorOf(Props(new TransferDomainService(UUID.randomUUID())))

  actor ! Transfer(UUID.randomUUID(), Money(JPY, 10), to, from)

  println(Await.result((to ? GetBalanceRequest(UUID.randomUUID())).mapTo[GetBalanceResponse], Duration.Inf))

  println(Await.result((from ? GetBalanceRequest(UUID.randomUUID())).mapTo[GetBalanceResponse], Duration.Inf))

}
