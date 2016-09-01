package com.neo.sk.hiStream.snake

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Terminated}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.neo.sk.hiStream.snake.Protocol.{GameMessage, GridDataSync}
import org.slf4j.LoggerFactory

/**
  * User: Taoz
  * Date: 8/29/2016
  * Time: 9:29 PM
  */


trait PlayGround {


  def joinGame(id: Long, name: String): Flow[String, GameMessage, Any]

}


object PlayGround {

  val log = LoggerFactory.getLogger(this.getClass)

  def craete(system: ActorSystem): PlayGround = {

    val ground = system.actorOf(Props(new Actor {
      var subscribers = Map.empty[Long, ActorRef]
      var snakes = Map.empty[Long, SnakeData]

      override def receive: Receive = {
        case r@Join(id, name, subscriber) =>
          log.debug(s"got $r")
          context.watch(subscriber)
          subscribers += (id -> subscriber)
          snakes += (id -> SnakeData(id, name))
          dispatch(Protocol.NewSnakeJoined(id, name))

        case r@Left(id, name) =>
          log.debug(s"got $r")
          subscribers.get(id).foreach( context.unwatch )
          subscribers -= id
          snakes -= id
          dispatch(Protocol.SnakeLeft(id, name))
        case r@Key(id, keyCode) =>
          log.debug(s"got $r")
          dispatch(Protocol.TextMsg(s"Aha! $id click [$keyCode]"))
          //TODO

        case r@Terminated(actor) =>
          log.debug(s"got $r")
          subscribers.find(_._2.equals(actor)).foreach{ case (id, _) =>
          log.debug(s"got Terminated id = $id")
            subscribers -= id
            snakes.get(id).foreach( s => dispatch(Protocol.SnakeLeft(id, s.name)))
          }
      }

      def dispatch(gameOutPut: GameMessage) = {
        subscribers.foreach { case (_, ref) => ref ! gameOutPut }
      }

      def syncGridData(gridData: GridDataSync) = {
        subscribers.foreach { case (id, ref) => ref ! gridData.copy(uid = id) }
      }

    }
  ), "ground")


  def playInSink(id: Long, name: String) = Sink.actorRef[UserAction](ground, Left(id, name))


  new PlayGround {

    override def joinGame(id: Long, name: String): Flow[String, Protocol.GameMessage, Any] = {

      val in =
        Flow[String]
          .map(s => Key(id, s.headOption.getOrElse(' ').toInt))
          .to(playInSink(id, name))

      val out =
        Source.actorRef[Protocol.GameMessage](3, OverflowStrategy.dropHead)
          .mapMaterializedValue(outActor => ground ! Join(id, name, outActor))

      Flow.fromSinkAndSource(in, out)
    }

  }

}


private sealed trait UserAction

private case class Join(id: Long, name: String, subscriber: ActorRef) extends UserAction

private case class Left(id: Long, name: String) extends UserAction

private case class Key(id: Long, keyCode: Int) extends UserAction


}