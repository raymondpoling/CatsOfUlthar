package cats.abstractcat

import com.typesafe.config.Config

/**
 * Created by ruguer on 3/20/15.
 */
abstract class Cat(val name:String,config: Config) {

  def act() : Unit
}
