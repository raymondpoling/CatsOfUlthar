package cats.realcat

import cats.abstractcat.Cat
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.hyperic.sigar.Sigar
import org.hyperic.sigar.cmd.Shell

import scala.util.Random

/**
 * Created by ruguer
 * 3/20/15.
 */
class PidCat(name:String,config:Config) extends Cat(name,config) with LazyLogging {

  val pattern = config.getString("pattern")
  val signals = config.getStringList("signals")

  val pid = new Shell

  override def act(): Unit = {
    val first = pid.findPids(s"State.Name.re=$pattern")
    val result = if(first.length!=0) first else pid.findPids(s"Args.*.re=$pattern")
    if(result.length==0) {
      logger.info(s"$name with $pattern couldn't find a pid!")
      return
    }
    val sigar = new Sigar
    val random = Random
    val selectPid = result(random.nextInt(result.size))
    val selectSignal = signals.get(random.nextInt(signals.size()))

    sigar.kill(selectPid,selectSignal)

    logger.info(s"$name with $pattern has $selectSignal pid $selectPid!")
  }
}
