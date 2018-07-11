package com.neo.sk.hiStream.chat

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.neo.sk.hiStream.chat.RoomMaster.{JoinRoom, LeftRoom}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/**
  * User: Taoz
  * Date: 7/10/2018
  * Time: 10:51 AM
  */
trait ChatRoom {

  def join(id: Int, name: String): Flow[String, String, Any]

}


object ChatRoom {

  import RoomMaster._

  private case class Key(id: Long, keyCode: Int)

  private case class NetTest(id: Long, createTime: Long)


  def playInSink(
    id: Int,
    name: String,
    room: ActorRef): Sink[String, NotUsed] = {
    Sink.actorRef[String](room, LeftRoom(id, name))
  }

  def create(system: ActorSystem)(implicit executor: ExecutionContext): ChatRoom = {

    val room = system.actorOf(RoomMaster.props(), "roomMaster")

    new ChatRoom {
      override def join(id: Int, name: String): Flow[String, String, Any] = {
        val in =
          Flow[String]
            .map { s => s }
            .to(playInSink(id, name, room))
        val out =
          Source.actorRef[String](3, OverflowStrategy.dropHead)
            .mapMaterializedValue(outActor => room ! JoinRoom(id, name, outActor))

        Flow.fromSinkAndSource(in, out)
      }
    }
  }
}

object RoomMaster {

  def props() = Props(new RoomMaster())

  final case class JoinRoom(id: Int, name: String, peer: ActorRef)

  final case class LeftRoom(id: Int, name: String)


}

class RoomMaster extends Actor {

  import concurrent.duration._

  private var peer: Option[ActorRef] = None
  private val log = LoggerFactory.getLogger(this.getClass)

  import context.dispatcher

  val keepAlive =
    context.system.scheduler.schedule(0 second, 3 second, self, "\u0001")

  override def receive: Receive = {
    case "\u0001" =>
      peer.foreach(_ ! "\u0001")
    case JoinRoom(id, name, userRef) =>
      log.info(s"$id joined room by [$name]")
      peer = Some(userRef)
      dispatch(s"welcome [$name]")
    case str: String =>
      log.info(s"got msg: $str")
      dispatch(s"i got your msg[$str]")
    case LeftRoom(_, name) =>
      log.info(s"$name left.")
      peer = None
    case x =>
      log.info(s"got unknown msg: $x")
      dispatch("I can not understand.")
  }


  private def dispatch(msg: String): Unit = {
    peer.foreach(_ ! msg)
  }
}
