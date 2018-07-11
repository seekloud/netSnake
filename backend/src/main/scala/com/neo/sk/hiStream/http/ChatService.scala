package com.neo.sk.hiStream.http

import java.util.concurrent.atomic.AtomicInteger

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.{getFromResource, handleWebSocketMessages, parameter, path, pathEndOrSingleSlash, pathPrefix}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorAttributes, Materializer, Supervision}
import akka.util.{ByteString, Timeout}
import com.neo.sk.hiStream.chat.ChatRoom
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor

/**
  * User: Taoz
  * Date: 7/10/2018
  * Time: 12:25 PM
  */
trait ChatService {


  import io.circe.syntax._

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit val timeout: Timeout

  lazy val chatRoom: ChatRoom = ChatRoom.create(system)

  private val idGenerator = new AtomicInteger(1000000)

  private[this] val log = LoggerFactory.getLogger("com.neo.sk.hiStream.http.SnakeService")


  val chatRoute: Route = {
    (pathPrefix("chat") & get) {
      pathEndOrSingleSlash {
        getFromResource("html/chat.html")
      } ~
      path("join") {
        parameter('name) { name =>
          println(s"Got a join connection: $name")
          handleWebSocketMessages(webSocketChatFlow(name))
        }
      }
    }
  }


  def webSocketChatFlow(nickname: String): Flow[Message, Message, Any] =
    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) =>
          log.info(s"msg from webSocket: $msg")
          msg
        case BinaryMessage.Strict(bMsg) =>


          val buffer = bMsg.asByteBuffer
          val len = buffer.get().toShort
          val bytes = new Array[Byte](len)
          println(s"msg bytes len: $len")
          for( i <- 0 until len){
            bytes(i) = buffer.get()
            println(s"get byte($i): ${bytes(i)}")
          }

          val msg = new String(bytes, "utf-8")




          //println(s"got BinaryMessage $msg, len=${msg.length} size=${bMsg.toList.length}")

          //TODO here.
          msg
        // unpack incoming WS text messages...
        // This will lose (ignore) messages not received in one chunk (which is
        // unlikely because chat messages are small) but absolutely possible
        // FIXME: We need to handle TextMessage.Streamed as well.
      }
      .via(chatRoom.join(idGenerator.getAndIncrement(), nickname)) // ... and route them through the chatFlow ...
      //.map { msg => TextMessage.Strict(msg) // ... pack outgoing messages into WS JSON messages ...
      .map { msg => BinaryMessage.Strict(ByteString(str2bytes(msg))) // ... pack outgoing messages into WS JSON messages ...
    }.withAttributes(ActorAttributes.supervisionStrategy(decider)) // ... then log any processing errors on stdin

  private def str2bytes(str: String): Array[Byte] = {
    val data = str.getBytes("utf-8")
    val len = data.length
    val array = new Array[Byte](len + 1)
    array(0) = len.toByte
    for(i <- 0 until len) {
      array(i + 1) = data(i)
    }
    println(s"str2bytes: str=[$str] to [${array.mkString(",")}]")
    array
  }


  private def tmp: Flow[Any, Nothing, NotUsed] = {
    val in = Sink.ignore
    val out = Source.empty
    Flow.fromSinkAndSource(in, out)
  }

  private val decider: Supervision.Decider = {
    e: Throwable =>
      e.printStackTrace()
      println(s"WS stream failed with $e")
      Supervision.Resume
  }


}
