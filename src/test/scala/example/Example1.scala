package example

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Cancellable}
import akka.stream.{ActorMaterializer, ClosedShape, Graph, Materializer}
import org.specs2.mutable.Specification
import output.EndPoint
import builder.EventGeneratorBuilder
import scala.collection.mutable
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{FiniteDuration, Duration}
import scala.util.{Success, Try, Failure}


/**
  * Created by ruguer on 12/28/15.
  */
class Example1 extends Specification {
  import Example1.PrintToQueue

  sealed trait State {
    val str : String
  }
  case class Seed(str:String) extends State
  case class Continue(str:String) extends State
  case class Reset(str:String) extends State
  case class Output(str:String)

  val example = new EventGeneratorBuilder[State,State,Output](Seed("s"))
    .noInput
    .simpleEventRate(Duration(100,TimeUnit.MILLISECONDS),Seed("t"))
    .generator {
    case (_, Seed(str)) =>
      (Continue(str), Output(str))
    case (Seed(o), Continue(str)) => (Continue(new StringBuilder(str).append(o).toString()), Output(str))
  }

  "Example" should {
    "create events at interval" in {
      val queue = new mutable.Queue[String]()
      implicit val system = ActorSystem("example1")
      implicit val mat = ActorMaterializer()
      example
        .output((t: Output) => t.str, new PrintToQueue(queue))
        .generator {
        case (_, Seed(str)) =>
          (Continue(str), Output(str))
        case (Seed(o), Continue(str)) => (Continue(new StringBuilder(str).append(o).toString()), Output(str))
      }.build.run
      eventually {
        queue.size must be_>(8)
      }
      mat.shutdown()
      success
    }
    "allow for two inputs, for separate discrete event sources" in {
      val queue = new mutable.Queue[String]()
      implicit val system = ActorSystem("example2")
      implicit val mat = ActorMaterializer()
      example
        .simpleEventRate(Duration(500,TimeUnit.MILLISECONDS),Reset("cat"))
        .output((t: Output) => t.str, new PrintToQueue(queue))
        .generator {
          case (_, Seed(str)) =>
            (Continue(str), Output(str))
          case (Reset(str),_) => (Continue(str),Output(str))
          case (Seed(o), Continue(str)) => (Continue(new StringBuilder(str).append(o).toString()), Output(str))
        }
        .build.run
      eventually {
        queue.contains("catt") must beTrue
      }
      mat.shutdown()
      success
    }
    "allow for two outputs, for separate discrete event sources" in {
      val queue1 = new mutable.Queue[String]()
      val queue2 = new mutable.Queue[String]()
      implicit val system = ActorSystem("example2")
      implicit val mat = ActorMaterializer()
      example
        .output((t: Output) => t.str, new PrintToQueue(queue1))
        .output((t: Output) => t.str, new PrintToQueue(queue2))
        .generator {
          case (_, Seed(str)) =>
            (Continue(str), Output(str))
          case (Seed(o), Continue(str)) => (Continue(new StringBuilder(str).append(o).toString()), Output(str))
        }
        .build.run
      eventually {
        queue1.size must be_>(8) and {queue1 must contain(queue2.toSet)}
      }
      mat.shutdown()
      success
    }
    "allow for merging generators" in {
      val queue1 = new mutable.Queue[String]()
      val queue2 = new mutable.Queue[String]()
      implicit val system = ActorSystem("example2")
      implicit val mat = ActorMaterializer()
      val example1 = example
        .output((t: Output) => {
          t.str
        }, new PrintToQueue(queue1))
        .generator {
          case (_, Seed(str)) =>
            (Continue(str), Output(str))
          case (Seed(o), Continue(str)) => (Continue(new StringBuilder(str).append(o).toString()), Output(str))
        }


      val example2 = EventGeneratorBuilder[Output,State,Output](Continue(""))
        .simpleEventRate(Duration(700,TimeUnit.MILLISECONDS),Output("mirku"))
        .output((t: Output) => t.str, new PrintToQueue(queue1))
        .generator {
          case (_, Seed(str)) =>
            (Continue(str), Output(str))
          case (Output(o), Continue(str)) => (Continue(o), Output(str))
        }
        .generatorSource(example1)
      example2.build.run
      eventually {
        queue1 must contain("mirku")
        queue1 must contain("stt")
      }
      mat.shutdown()
      success
    }
  }
}
object Example1 {
  class PrintToQueue[T](queue:mutable.Queue[T]) extends EndPoint[T] {
    override def write(output: T): Unit = {
      queue.enqueue(output)
    }
  }
}