package app

import java.io.File
import java.net.InetAddress

import akka.actor.{Props, ActorSystem}
import akka.pattern._
import akka.io.IO
import akka.util.Timeout
import cattamer.CatMasterGeneral
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import routes.Routes
import spray.can.Http
import scala.concurrent.duration._

/**
 * Created by ruguer on 3/25/15.
 */
object Boot extends LazyLogging {

  def toParameters(args:Array[String]) : Map[String,String] = {
    println(s"args: ${args.mkString(", ")}")
    args.map(_.span(j => !'='.equals(j))).map(i => (i._1,i._2.dropWhile('='.equals))).toMap[String,String]
  }

  def main(args:Array[String]) : Unit = {
    // we need an ActorSystem to host our application in
    implicit val system = ActorSystem("on-spray-can")

    val cfgFile = toParameters(args)("config")

    val config = ConfigFactory.parseFile(new File(cfgFile))

    logger.info("Config is {}, starting on {}:{}",config,config.getString("host"),config.getInt("port").toString)

    // create and start our service actor
    val service = system.actorOf(Props(classOf[Routes],config.getObject("cats")), "service")

    implicit val timeout = Timeout(5.seconds)
    // start a new HTTP server on port 8080 with our service actor as the handler
    IO(Http) ? Http.Bind(service, interface = config.getString("host"), port = config.getInt("port"))
  }
}
