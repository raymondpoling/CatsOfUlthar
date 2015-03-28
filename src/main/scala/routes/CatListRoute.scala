package routes

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import spray.http.HttpHeaders.`Content-Type`
import spray.http.MediaTypes._
import spray.httpx.marshalling._
import spray.json._
import spray.json.DefaultJsonProtocol._
import spray.routing._
import cattamer.CatMasterGeneral._


import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success
import spray.httpx.SprayJsonSupport._
import spray.util._
import spray.httpx.marshalling.MetaMarshallers._


/**
 * Created by ruguer
 * 3/25/15.
 */
trait CatListRoute extends HttpService with MetaMarshallers {
  self : RequireMasterGeneral =>

  implicit val timeout = Timeout(10.seconds)

  import scala.concurrent.ExecutionContext.Implicits.global

  val allCats : Route =
    path("listCats") {
      get {
      respondWithMediaType(`application/json`) {
        complete {
          (catMasterGeneral ? CatList).mapTo[AllCats].map(t => JsObject("cats" -> JsArray(t.catNames.map(JsString.apply).toVector)))
        }
      }
    }
  }
}
