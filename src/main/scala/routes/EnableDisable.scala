package routes

import cattamer.CatMasterGeneral.Request
import cattamer.CatTamer.{Disable, Enable}
import spray.http.MediaTypes._
import spray.httpx.marshalling.MetaMarshallers
import spray.routing.PathMatchers.Segment
import spray.routing._

/**
 * Created by ruguer on 5/1/15.
 */
trait EnableDisable extends HttpService with MetaMarshallers {
  self : RequireMasterGeneral =>

  val enableDisableRoute : Route = pathPrefix(Segment) { (catName) =>
    path("enable") {
      put {
        respondWithMediaType(`application/json`) {
          complete {
            catMasterGeneral ! Enable(catName)
            s"""{"msg":"Asked cat $catName to enable"}"""
          }
        }
      }
    } ~
      path("disable") {
        put {
          respondWithMediaType(`application/json`) {
            complete {
              catMasterGeneral ! Disable(catName)
              s"""{"msg":"Asked cat $catName to diable"}"""
            }
          }
        }
      }
  }
}
