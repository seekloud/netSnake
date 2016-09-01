package com.neo.sk.hiStream.snake

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/**
  * User: Taoz
  * Date: 8/29/2016
  * Time: 9:29 PM
  */


trait PlayGround {


  def joinGame(id: Long, name: String): Flow[String, Protocol.GameMessage, Any]

  def syncData()

}


object PlayGround {

  val bounds = Point(300, 150)

  val log = LoggerFactory.getLogger(this.getClass)

  def create(system: ActorSystem)(implicit executor: ExecutionContext): PlayGround = {

    val ground = system.actorOf(Props(new Actor {
      var subscribers = Map.empty[Long, ActorRef]

      val grid = new Grid(bounds)
      override def receive: Receive = {
        case r@Join(id, name, subscriber) =>
          log.debug(s"got $r")
          context.watch(subscriber)
          subscribers += (id -> subscriber)
          grid.addSnake(id, name)
          dispatch(Protocol.NewSnakeJoined(id, name))

        case r@Left(id, name) =>
          log.debug(s"got $r")
          subscribers.get(id).foreach(context.unwatch)
          subscribers -= id
          grid.removeSnake(id)
          dispatch(Protocol.SnakeLeft(id, name))
        case r@Key(id, keyCode) =>
          //log.debug(s"got $r")
          dispatch(Protocol.TextMsg(s"Aha! $id click [$keyCode]"))//just for test
          grid.addAction(id, keyCode)

        case r@Terminated(actor) =>
          log.debug(s"got $r")
          subscribers.find(_._2.equals(actor)).foreach { case (id, _) =>
            log.debug(s"got Terminated id = $id")
            subscribers -= id
            grid.removeSnake(id).foreach(s => dispatch(Protocol.SnakeLeft(id, s.name)))
          }
        case Sync =>
          val gridData = grid.updateAndGetGridData()
          syncGridData(gridData)
        case x =>
          log.warn(s"got unknown msg: $x")
      }

      def dispatch(gameOutPut: Protocol.GameMessage) = {
        subscribers.foreach { case (_, ref) => ref ! gameOutPut }
      }

      def syncGridData(gridData: GridDataSync) = {
        subscribers.foreach { case (id, ref) => ref ! Protocol.GridDataMessage(id, gridData) }
      }

    }
    ), "ground")

    import concurrent.duration._
    system.scheduler.schedule(3 seconds, 1000 millis, ground, Sync )// sync tick


    def playInSink(id: Long, name: String) = Sink.actorRef[UserAction](ground, Left(id, name))


    new PlayGround {
      override def joinGame(id: Long, name: String): Flow[String, Protocol.GameMessage, Any] = {
        val in =
          Flow[String]
            .map(s => Key(id, s.toInt))
            .to(playInSink(id, name))

        val out =
          Source.actorRef[Protocol.GameMessage](3, OverflowStrategy.dropHead)
            .mapMaterializedValue(outActor => ground ! Join(id, name, outActor))

        Flow.fromSinkAndSource(in, out)
      }

      override def syncData(): Unit = ground ! Sync
    }

  }


  private sealed trait UserAction
  private case class Join(id: Long, name: String, subscriber: ActorRef) extends UserAction
  private case class Left(id: Long, name: String) extends UserAction
  private case class Key(id: Long, keyCode: Int) extends UserAction
  private case object Sync extends UserAction


}