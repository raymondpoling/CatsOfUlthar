package routes

import cattamer.CatMasterGeneral.Request
import spray.httpx.marshalling.MetaMarshallers
import spray.routing.{Route, HttpService}

import spray.http.MediaTypes._

/**
 * Created by ruguer on 3/25/15.
 */
trait AskCatToAct extends HttpService with MetaMarshallers {
  self : RequireMasterGeneral =>

  val askRoute : Route = path(Segment) { (catName) =>
    get {
      respondWithMediaType(`application/json`) {
        complete {
          catMasterGeneral ! Request(catName)
          s"""{"msg":"Asked cat $catName to act"}"""
        }
      }
    }
  }

}
