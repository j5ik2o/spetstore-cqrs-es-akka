package controllers

import javax.inject._

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import akka.util.Timeout
import com.github.j5ik2o.spetstore.adaptor.http.{CreateCustomerJson, CustomerSupport}
import com.github.j5ik2o.spetstore.usecase.CustomerUseCase
import com.github.tototoshi.play2.json4s.Json4s
import org.json4s._
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class CustomerController @Inject()(json4s: Json4s, @Named("customer-aggregate") customerAggregate: ActorRef)(implicit exec: ExecutionContext, actorSystem: ActorSystem, meterializer: Materializer)
  extends Controller with CustomerSupport {

  import json4s._

  implicit val formats = DefaultFormats

  implicit val timeout = Timeout(10 seconds)

  override val customerUseCase: CustomerUseCase = CustomerUseCase(customerAggregate)

  def create: Action[JValue] = Action.async(json) { implicit request =>
    val createCustomerJson = request.body.extract[CreateCustomerJson]
    createCustomerGraph(createCustomerJson).run().map(e => Ok(Extraction.decompose(e)))
  }

}
