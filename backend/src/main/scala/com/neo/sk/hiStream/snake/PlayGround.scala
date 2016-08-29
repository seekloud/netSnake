package com.neo.sk.hiStream.snake

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.stream.scaladsl.Flow

/**
  * User: Taoz
  * Date: 8/29/2016
  * Time: 9:29 PM
  */




trait PlayGround {


  def joinGame(id: Long, name: String): Flow[String, GameOutPut, Any]

}


object PlayGround {


  def craete(system: ActorSystem): PlayGround = {


    val ground = system.actorOf(Props(new Actor{

      var subscribers = Map.empty[Long, ActorRef]
      var snakes = Map.empty[Long, SnakeData]
      override def receive: Receive = {
        case Join(id, name, subscriber) =>
        case Left(id, name) =>
        case Key(id, keyCode) =>
        case Terminated(actor) =>
      }
    }),"ground")



    null
  }







  private case class Join(id: Long, name: String, subscriber: ActorRef)
  private case class Left(id: Long, name: String)
  private case class Key(id: Long, keyCode: Int)


}