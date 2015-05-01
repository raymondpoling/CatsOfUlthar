package cattamer

import java.io.File
import java.util.concurrent.TimeUnit

import akka.actor.{Cancellable, Props, Actor}
import cats.abstractcat.Cat
import cattamer.CatTamer.{Disable, Enable}
import com.typesafe.config.{ConfigFactory, Config}
import com.typesafe.scalalogging.LazyLogging
import message.Act
import scala.concurrent.duration._
import scala.reflect.ClassTag

import scala.util.Random

/**
 * Created by ruguer on 3/24/15.
 */
class CatTamer(myCat:Cat,returnPeriodInSeconds:Long,var enabled:Boolean) extends Actor with LazyLogging {

  def nextPeriod = (Math.log(1-Random.nextDouble())/(-1/returnPeriodInSeconds.toDouble)).toLong

  import scala.concurrent.ExecutionContext.Implicits.global

  def actInTheFuture(wait:Long) : Option[Cancellable] = if(enabled) {
    logger.info("{} will act in {} ({} seconds).",myCat.name,pretty(wait),wait.toString)
    val t = context.system.scheduler.scheduleOnce(wait seconds,self, ActOnMyOwn)
    Some(t)
  } else None

  logger.info(s"${myCat.name} return period is $returnPeriodInSeconds")

  var nextEvent = actInTheFuture(nextPeriod)

  override def receive: Receive = {
    case Enable(_) => enabled = true
      if(nextEvent.isEmpty)
        nextEvent = actInTheFuture(nextPeriod)
      logger.info(s"${myCat.name} is Enabled")
    case Disable(_) => enabled = false
      nextEvent = nextEvent.flatMap(t => {t.cancel();None})
      logger.info(s"${myCat.name} is Disabled")
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
    val isEnabled = if(config.hasPath("isEnabled")) config.getBoolean("isEnabled") else true
    val duration = config.getDuration("returnPeriod",TimeUnit.SECONDS)
    Props(classOf[CatTamer],cat,duration,isEnabled)
  }

  trait IsEnabled
  case class Enable(name:String) extends IsEnabled
  case class Disable(name:String) extends IsEnabled
}