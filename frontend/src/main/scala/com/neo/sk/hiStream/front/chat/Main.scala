package com.neo.sk.hiStream.front.chat

import com.neo.sk.frontUtils.{Component, MiddleBufferInJs}
import com.neo.sk.hiStream.chat.Protocol.{Msg, MultiTextMsg, TextMsg}
import mhtml._
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.{Input, TextArea}
import org.scalajs.dom.raw._

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js.typedarray.ArrayBuffer
import scala.xml.Elem

/**
  * User: Taoz
  * Date: 7/10/2018
  * Time: 4:15 PM
  */
@JSExportTopLevel("chat.Main")
object Main {

  @JSExport
  def run(): Unit = {
    show()
    println("hello world, i am a chat room.")
  }
  def show(): Unit = {
    val page = MainPage.render
    mount(dom.document.body, page)
  }
}

object MainPage extends Component {

  import com.neo.sk.frontUtils.byteObject.ByteObject._


  private var username = ""
  private var messageInput: Option[Input] = None
  private var boardElement: Option[TextArea] = None
  private var wsConnection: Option[WebSocket] = None
  private val messageBoard: Var[String] = Var("")

  private def getWebSocketUri(nameOfChatParticipant: String): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host}/hiStream/chat/join?name=$nameOfChatParticipant"
  }

  private def joinRoom(name: String): Unit = {
    if (wsConnection.exists(_.readyState < 2)) {
      println(s"ws is ok, no need to setup: ${wsConnection.map(_.readyState).getOrElse("NULL")}")
    } else {
      println(s"$name click join button.")
      val wsUrl = getWebSocketUri(name)
      println(s"wsUrl: $wsUrl")
      val con = new WebSocket(wsUrl)
      con.binaryType = "blob"
      wsConnection = Some(con)
      con.onopen = wsOnOpen _
      con.onmessage = wsOnMessage _
      con.onclose = wsOnClose _
      con.onerror = wsOnError _
    }

  }


  private def wsOnOpen(ev: Event): Unit = {
    println("wsOnOpen:" + ev.`type`)
  }


  private def wsOnMessage(ev: MessageEvent): Unit = {
    ev.data match {
      case stringMsg: String =>
        println("wsOnMessage:" + stringMsg)
        if (stringMsg != "\u0001") {
          messageBoard.update { current =>
            current + "\n" +
            s"$username: " + stringMsg
          }
        }
      case blobMsg: Blob =>
        println(s"got blob msg: $blobMsg")

        val fr = new FileReader()
        fr.readAsArrayBuffer(blobMsg)
        fr.onloadend = { _: Event =>
          val buf = fr.result.asInstanceOf[ArrayBuffer]
          println(s"load length: ${buf.byteLength}")
          /*
                    val b = new Int8Array(buf)
                    println(s"b length: ${b.length}")
                    for (i <- 0 until 10) {
                      //            val s = Integer.toHexString(b.get(i) & 0xFF)
                      println(s"[$i] byte: [${b.get(i)}]")
                    }
          */
          val middleDataInJs = new MiddleBufferInJs(buf)

          bytesDecode[Msg](middleDataInJs) match {
            case Right(data) => data match {
              case m@TextMsg(id, data, value, dd) =>
                println(s"got m=$m")
                messageBoard.update { current =>
                  current + "\n" +
                  s"$username: " + data
                }
              case m@MultiTextMsg(id, d, ls) =>
                println(s"got m=$m")
                val msg = ls.map { r =>
                  s"$username: m[" + r.data + "]"
                }.mkString("\n")
                messageBoard.update { current =>
                  current + "\n" + msg
                }
            }
            case Left(error) =>
              println(s"got error: ${error.message}")
          }


          /*          val data = TestMessage.decode(middleDataInJs)
                    val msg = data.data

                    println(s"msg: ${data.id}")
                    println(s"msg: ${data.data}")
                    println(s"msg: ${data.ls.mkString(",")}")
                    if (msg != "\u0001") {
                      messageBoard.update { current =>
                        current + "\n" +
                        s"$username: " + msg
                      }
                    }
                    */


          /*
          val bs = new Uint8Array(buf)
          val len = bs.get(0)
          val bytes = new Array[Byte](len)
          println(s"msg bytes len: $len")
          for (i <- 0 until len) {
            bytes(i) = bs.get(i + 1).toByte
            println(s"get byte($i): ${bytes(i)}")
          }
          val msg = new String(bytes, "utf-8")
          println(s"decoded msg: [$msg]")
          if (msg != "\u0001") {
            messageBoard.update { current =>
              current + "\n" +
              s"$username: " + msg
            }
          }*/


        }

      case abMsg: ArrayBuffer =>
        println(s"got abMsg msg: $abMsg")

      case x => println(s"got unknown msg: $x")

    }

  }


  private def wsOnError(ev: Event): Unit = {
    println("wsOnError:" + ev.`type`)
  }


  private def wsOnClose(ev: CloseEvent): Unit = {
    println(s"wsOnClose: [${ev.code}, ${ev.reason}, ${ev.wasClean}]")
  }


  val sendBuffer = new MiddleBufferInJs(2048)


  private def sendMessage(): Unit = {
    val input = messageInput.map(_.value).getOrElse("NULL")

    //    wsConnection.foreach(_.send(msg))


    /*    wsConnection.foreach { ws =>
          val data = msg.getBytes("utf-8")

          val a = js.Array(data)
          val b = js.JSArrayOps(data)
          val blobMsg = new Blob(js.Array(), BlobPropertyBag("application/octet-stream"))
          ws.send(blobMsg)
        }*/
    wsConnection.foreach { ws =>

      val id = (System.currentTimeMillis() / 10000).toInt
      val ls = scala.collection.immutable.Range(0, id % 10 + 2, 1).map(_ + 0.1f).toArray

      val msg: Msg =
        if (input.startsWith("3x")) {
          val ls = (1 to 3).map { i =>
            TextMsg(id, input, 0.1f * i, 1.0000000000001)
          }
          MultiTextMsg(id, Some(true), ls.toList)
        } else {
          TextMsg(id, input, id.toFloat / 1000, 1.0000000000000003)
        }

      //test error msg.
      //val msg = TextMsg(id, input, id.toFloat / 1000)

      msg.fillMiddleBuffer(sendBuffer)
      val ab: ArrayBuffer = sendBuffer.result()

      println(s"send msg: $msg")


      /*
      val id = (System.currentTimeMillis() / 10000).toInt
      val ls =  scala.collection.immutable.Range(0, id % 10 + 2, 1).map( _ + 0.1f).toArray

      val testMessage = TestMessage(id, msg, ls)
      sendBuffer.clear()
      testMessage.encode(sendBuffer)
      val ab = sendBuffer.result()

      println(s"send test message, id=${testMessage.id}")
      println(s"send test message, data=${testMessage.data}")
      println(s"send test message, ls=${testMessage.ls.mkString(",")}")


      * */

      /*
          import js.JSConverters._
            val data = msg.getBytes("utf-8")
            val len = data.size.toShort
            val ab = new js.typedarray.ArrayBuffer(len + 1)
            val bs = new Uint8Array(ab)
            bs.set(0, len)
            println(s"set len to: $len")
            println(s"set all data at once.")
            bs.set(data.toJSArray, 1)
      */

      //          for (i <- 0 until len) {
      //            println(s"set $i to ${data(i)}")
      //            bs.set(i + 1, data(i))
      //          }
      ws.send(ab)
    }

    boardElement.foreach { b =>
      b.scrollTop = b.scrollHeight
    }
    messageInput.foreach(_.value = "")
  }

  override def render: Elem = {
    <div>
      <h1>Welcome!</h1>
      <input placeholder="nickname"
             onchange={e: Event => username = e.target.asInstanceOf[dom.html.Input].value}>
        {username}
      </input>
      <button onclick={() => joinRoom(username)}>Join</button>
      <div>
        <textarea style="padding:10px; margin: 10px 0px; box-sizing: border-box; border-style: solid; width: 300px; height: 200px;"
                  mhtml-onmount={e: TextArea => boardElement = Some(e)}
                  readonly="readonly">
          message board:
          {messageBoard}
        </textarea>
      </div>


      <input style="width:300px"
             onkeydown={e: KeyboardEvent => if (e.keyCode == KeyCode.Enter) sendMessage()}
             mhtml-onmount={e: Input => messageInput = Some(e)}>
      </input>
      <button onclick={() => sendMessage()}>send</button>
    </div>
  }


}


object WsConnection {

  val gameStream = new WebSocket("")

  def connect(): Unit = {

  }

}

