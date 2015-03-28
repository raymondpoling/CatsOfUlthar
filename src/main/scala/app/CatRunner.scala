package app

import java.io.File

import cats.abstractcat.Cat
import com.typesafe.config.{Config, ConfigFactory}

/**
 * Created by ruguer on 3/25/15.
 */
object CatRunner {
  def toParameters(args:Array[String]) : Map[String,String] = {
    println(s"args: ${args.mkString(", ")}")
    args.map(_.span(j => !'='.equals(j))).map(i => (i._1,i._2.dropWhile('='.equals))).toMap[String,String]
  }

  def main(args:Array[String]) : Unit = {
    val param = toParameters(args)
    println(s"Parameters: $param")
    val cfg = param("config")
    val wantCat = param("name")
    val configuration = ConfigFactory.parseFile(new File(cfg))
    println(s"Configuration: $configuration")
    val catClass = this.getClass.getClassLoader.loadClass(configuration.getConfig("cats").getConfig(wantCat).getString("class"))
    val cat = catClass.getConstructor(classOf[String],classOf[Config])
    cat.newInstance(wantCat,configuration.getConfig(wantCat)).asInstanceOf[Cat].act()

  }
}
