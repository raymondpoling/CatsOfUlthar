package routes

import akka.actor.ActorRef
import cattamer.CatMasterGeneral

/**
 * Created by ruguer on 3/25/15.
 */
trait RequireMasterGeneral {
  val catMasterGeneral : ActorRef
}
