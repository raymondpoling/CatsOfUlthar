package cats.realcat


import cats.abstractcat.Cat
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scalaj.http.Http

/**
 * Created by ruguer on 4/4/15.
 */
class UrlCat(name:String,config:Config) extends Cat(name,config) with LazyLogging {
  override def act(): Unit = {
    val url = config.getString("url")

    val data = if(config.hasPath("data")) config.getString("data") else ""

    val method = (if(config.hasPath("method")) config.getString("method") else "GET").toUpperCase

    val result = Http(url).method(method).postData(data).asString

    logger.info("Connectiong to {} with method {} and got {}.",url,method,result)


  }
}
