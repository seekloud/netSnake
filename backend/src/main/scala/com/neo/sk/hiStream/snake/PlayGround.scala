package com.neo.sk.hiStream.snake

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}

/**
  * User: Taoz
  * Date: 8/29/2016
  * Time: 9:29 PM
  */


trait PlayGround {


  def joinGame(id: Long, name: String): Flow[String, GameOutPut, Any]

}


object PlayGround {


  //TODO here
  def craete(system: ActorSystem): PlayGround = {


    val ground = system.actorOf(Props(new Actor {
      var subscribers = Map.empty[Long, ActorRef]
      var snakes = Map.empty[Long, SnakeData]

      val a = (1, 2)
      val b = 1 -> 2


      override def receive: Receive = {
        case Join(id, name, subscriber) =>
          context.watch(subscriber)
          subscribers += (id -> subscriber)
          snakes +=  (id -> SnakeData(id, name))

        case Left(id, name) =>
        case Key(id, keyCode) =>

        case Terminated(actor) =>

      }
    }), "ground")


    def playInSink(id: Long, name: String) = Sink.actorRef[UserAction](ground, Left(id, name))


    new PlayGround {

      override def joinGame(id: Long, name: String): Flow[String, GameOutPut, Any] = {

        val in =
          Flow[String]
            .map(s => Key(id, s.headOption.getOrElse(' ').toInt))
            .to(playInSink(id, name))

        val out =
          Source.actorRef[GameOutPut](3, OverflowStrategy.dropHead)
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