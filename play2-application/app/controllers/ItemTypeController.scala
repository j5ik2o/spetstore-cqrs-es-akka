package controllers

import javax.inject.{ Inject, Named, Singleton }

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.Materializer
import akka.util.Timeout
import com.github.j5ik2o.spetstore.adaptor.http.{ CreateItemTypeJson, ItemTypeSupport }
import com.github.j5ik2o.spetstore.usecase.ItemTypeUseCase
import com.github.tototoshi.play2.json4s.Json4s
import org.json4s._
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class ItemTypeController @Inject() (json4s: Json4s, @Named("item-type-aggregate") itemTypeAggregate: ActorRef)(implicit exec: ExecutionContext, actorSystem: ActorSystem, meterializer: Materializer)
    extends Controller with ItemTypeSupport {
  import json4s._

  implicit val formats = DefaultFormats

  implicit val timeout = Timeout(10 seconds)

  override val itemTypeUseCase: ItemTypeUseCase = ItemTypeUseCase(itemTypeAggregate)

  def create: Action[JValue] = Action.async(json) { implicit request =>
    val createItemTypeJson = request.body.extract[CreateItemTypeJson]
    createItemTypeGraph(createItemTypeJson).run().map(e => Ok(Extraction.decompose(e)))
  }
}
