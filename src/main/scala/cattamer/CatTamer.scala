package cattamer

import java.io.File
import java.util.concurrent.TimeUnit

import akka.actor.{Props, Actor}
import cats.abstractcat.Cat
import com.typesafe.config.{ConfigFactory, Config}
import com.typesafe.scalalogging.LazyLogging
import message.Act
import scala.concurrent.duration._

import scala.util.Random

/**
 * Created by ruguer on 3/24/15.
 */
class CatTamer(myCat:Cat,returnPeriodInSeconds:Long) extends Actor with LazyLogging {

  def nextPeriod = (Math.log(1-Random.nextDouble())/(-1/returnPeriodInSeconds.toDouble)).toLong

  import scala.concurrent.ExecutionContext.Implicits.global

  def actInTheFuture(wait:Long) = {
    logger.info("{} will act in {} ({} seconds).",myCat.name,pretty(wait),wait.toString)
    context.system.scheduler.scheduleOnce(wait seconds,self, ActOnMyOwn)
  }

  println(s"${myCat.name} return period is $returnPeriodInSeconds")

  actInTheFuture(nextPeriod)

  override def receive: Receive = {
    case ActOnMyOwn =>
      logger.info("{} will be acting now.",myCat.name)
      myCat.act()
      actInTheFuture(nextPeriod)
    case Act =>
      logger.info("{} was asked to act now.",myCat.name)
      myCat.act()
  }

  case object ActOnMyOwn

  def pretty(nextEvent:Long) : String = {
    val seconds = nextEvent % 60
    val minutes = (nextEvent / 60) % 60
    val hours = (nextEvent / (60 * 60)) % 24
    val days = (nextEvent / (60 * 60) / 24) % 7
    val weeks = nextPeriod / (60 * 60) / 24 / 7
    f"${weeks}w ${days}d $hours%02d:$minutes%02d:$seconds%02d}"
  }
}

object CatTamer {
  def props(name:String,config:Config) : Props = {
    val catClass = this.getClass.getClassLoader.loadClass(config.getString("class"))
    val catConstructor = catClass.getConstructor(classOf[String],classOf[Config])
    val cat = catConstructor.newInstance(name,config).asInstanceOf[Cat]
    Props(classOf[CatTamer],cat,config.getDuration("returnPeriod",TimeUnit.SECONDS))
  }
}