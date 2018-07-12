package com.neo.sk.hiStream.http

import java.nio.ByteBuffer
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
import com.neo.sk.hiStream.chat.{ChatRoom, MiddleBufferInJvm}
import com.neo.sk.hiStream.chat.Protocol.TestMessage
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

          val middleData = new MiddleBufferInJvm(buffer)
          val testMessage = TestMessage.decode(middleData)

          println(s"test msg decode, id=${testMessage.id}")
          println(s"test msg decode, data=${testMessage.data}")
          println(s"test msg decode, ls=${testMessage.ls.mkString(",")}")

          val msg = testMessage.data

          /*
          val buffer = bMsg.asByteBuffer
          val len = buffer.get().toShort
          val bytes = new Array[Byte](len)
          println(s"msg bytes len: $len")
          for( i <- 0 until len){
            bytes(i) = buffer.get()
            println(s"get byte($i): ${bytes(i)}")
          }
          val msg = new String(bytes, "utf-8")*/

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
      .map { msg => BinaryMessage.Strict(ByteString(str2byteBuffer(msg))) // ... pack outgoing messages into WS JSON messages ...
    }.withAttributes(ActorAttributes.supervisionStrategy(decider)) // ... then log any processing errors on stdin


  var c = 10


  private def str2byteBuffer(str: String) = {
    val id = (System.currentTimeMillis() / 10000).toInt
    println(s"id: $id")
    val ls = new Range(0, id % 5, 1).toArray.map( _ + 0.1f)
    val msg = TestMessage(id, str, ls)
    val middleData = new MiddleBufferInJvm(256)
    TestMessage.encode(msg, middleData)
    val r = middleData.result()
    println(s"send msg: $msg")
    println(s"send bytes: ${r.mkString(",")}")
    r
  }


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
