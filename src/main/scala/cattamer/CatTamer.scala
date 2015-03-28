package cattamer

import java.io.File

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

  val prob : Double = (1/returnPeriodInSeconds.toDouble) * Math.pow(1 - (1/returnPeriodInSeconds.toDouble), returnPeriodInSeconds - 1)

  def nextPeriod = probStream.takeWhile(t => !t).size

  import scala.concurrent.ExecutionContext.Implicits.global

  def actInTheFuture(wait:Long) = {
    logger.info("{} will act in {} seconds.",myCat.name,wait.toString)
    context.system.scheduler.scheduleOnce(wait seconds,self, ActOnMyOwn)
  }

  println(s"${myCat.name} prob is $prob")

  def probStream : Stream[Boolean] = (Random.nextDouble() < prob) #:: probStream

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
}

object CatTamer {
  def props(name:String,config:Config) : Props = {
    val catClass = this.getClass.getClassLoader.loadClass(config.getString("class"))
    val catConstructor = catClass.getConstructor(classOf[String],classOf[Config])
    val cat = catConstructor.newInstance(name,config).asInstanceOf[Cat]
    Props(classOf[CatTamer],cat,config.getLong("returnPeriodInSeconds"))
  }
}