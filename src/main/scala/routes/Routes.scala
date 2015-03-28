package routes

import akka.actor.{Actor, ActorRefFactory, Props}
import cattamer.CatMasterGeneral
import com.typesafe.config.{ConfigObject, Config}
import spray.http.HttpHeaders.`Content-Type`
import spray.http.MediaTypes._
import spray.httpx.marshalling._
import spray.routing.{RoutingSettings, ExceptionHandler, HttpService, Route}
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Success
import spray.httpx.SprayJsonSupport._
import spray.util._
import spray.httpx.marshalling.MetaMarshallers._
import spray.json._


/**
 * Created by ruguer
 * 3/25/15.
 */
class Routes(config: ConfigObject) extends Actor with CatListRoute with AskCatToAct with RequireMasterGeneral {
  val catMasterGeneral = context.actorOf(CatMasterGeneral.props(config))

    implicit val exceptionHandler : ExceptionHandler = ExceptionHandler.default

  implicit val routeSetting : RoutingSettings = RoutingSettings.default(context)

  override def receive: Receive = runRoute(allCats ~ askRoute)

  override implicit def actorRefFactory: ActorRefFactory = context
}

object Routes {
  def props(config:Config) : Props = Props(classOf[Routes],config)
}