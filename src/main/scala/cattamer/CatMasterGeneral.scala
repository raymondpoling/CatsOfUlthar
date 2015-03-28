package cattamer

import akka.actor.{Actor, Props}
import cattamer.CatMasterGeneral.{AllCats, CatList, Request}
import com.typesafe.config.{Config, ConfigObject}
import message.Act

import scala.collection.JavaConverters._

/**
 * Created by ruguer on 3/25/15.
 */
class CatMasterGeneral(config:ConfigObject) extends Actor {

  val cats = config.keySet().asScala.map(k => k -> context.system.actorOf(CatTamer.props(k,config.toConfig.getConfig(k)))).toMap

  override def receive: Receive = {
    case Request(name) => cats.get(name).map(_ ! Act)
    case CatList => sender ! AllCats(cats.keySet.toList)
  }
}

object CatMasterGeneral {
  case class Request(name:String)
  case object CatList
  case class AllCats(catNames:List[String])

  def props(config:ConfigObject) : Props = {
    Props(classOf[CatMasterGeneral],config)
  }
}