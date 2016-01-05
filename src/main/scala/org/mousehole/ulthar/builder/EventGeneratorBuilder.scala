package org.mousehole.ulthar.builder

import akka.stream._
import akka.stream.scaladsl._
import org.mousehole.ulthar.output.EndPoint

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by ruguer on 12/28/15.
  */
case class EventGeneratorBuilder[Event,State,Output](
  initialState : State,

  output : List[Output => Output] = Nil,

  input : List[Graph[SourceShape[Event],_]] = Nil,

//  graphs : List[Graph[SourceShape[Event],_]] = Nil,

  gen : Option[State => Event => Output] = None) {

  def output[T](stateToString: (Output) => T, endpoint: EndPoint[T]) : EventGeneratorBuilder[Event,State,Output] = {
    this.copy(output = ((t: Output) => {
      (stateToString andThen endpoint.write) (t)
      t
    }) :: output)
  }

  def generator(value: PartialFunction[(Event,State),(State,Output)]) : EventGeneratorBuilder[Event,State,Output] = {
    this.copy(gen = Some((iState:State) => {
      var internalState: State = iState
      (event: Event) =>
        val (state, ret) = value((event, internalState))
        internalState = state
        ret
    }))
  }

  def simpleEventRate(duration: FiniteDuration, event:Event) : EventGeneratorBuilder[Event,State,Output] = {
    this.copy(input = Source.tick(duration,duration,event)::input)
  }

  def noInput : EventGeneratorBuilder[Event,State,Output] = {
    this.copy(input = Nil)
  }

  def build()(implicit ec : ExecutionContext) : RunnableGraph[_] =  RunnableGraph.fromGraph {
    GraphDSL.create(partialBuild()) {
      implicit b =>
        source =>
          import GraphDSL.Implicits._
          source ~> Sink.ignore
          ClosedShape
    }
  }

  private def partialBuild()(implicit ec : ExecutionContext) : Graph[SourceShape[Output],_] =  GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._
    val genStage = Flow[Event].map[Output](gen.get(initialState))
    val start = input match {
      case Nil =>
        Source.fromIterator(() => List().toIterator) ~> genStage
      case l::Nil =>
        val in = b.add(l)
        in ~> genStage
      case l =>
        val merge = b.add(Merge[Event](l.length))
        for (in <- l) {
          val ins = b.add(in)
          ins ~> merge
        }
        merge ~> genStage
    }
    val out = output.foldLeft(start)((acc,i) => acc.mapAsync(5)(t => {
      Future(i(t))
    }))
    SourceShape(out.outlet)
  }

  def generatorSource[GEvent,GState](otherGenerator: EventGeneratorBuilder[GEvent, GState, Event])(implicit ec : ExecutionContext) = {
    this.copy(input = otherGenerator.partialBuild()::input)
  }

  def createEventN(i: Int, seed: Event) : EventGeneratorBuilder[Event,State,Output] = {
    def iterator() = new Iterator[Event] {
      var count = 0
      override def hasNext: Boolean = count < i

      override def next(): Event = {
        count += 1
        seed
      }
    }
    this.copy(input = Source.fromIterator(iterator)::input)
  }
}
