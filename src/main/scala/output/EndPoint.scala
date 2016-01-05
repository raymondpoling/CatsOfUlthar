package output

/**
  * Created by ruguer on 12/25/15.
  */
trait EndPoint[T] {
  def write(output:T) : Unit
}
