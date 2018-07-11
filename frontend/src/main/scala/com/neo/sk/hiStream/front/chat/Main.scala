package com.neo.sk.hiStream.front.chat

import com.neo.sk.hiStream.front.snake.NetGameHolder.getWebSocketUri
import com.neo.sk.hiStream.front.utils.Component
import org.scalajs.dom

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import mhtml._
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.{Input, TextArea}
import org.scalajs.dom.raw._

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
    if(wsConnection.exists(_.readyState >= 2)){
      println(s"$name click join button.")
      val wsUrl = getWebSocketUri(name)
      println(s"wsUrl: $wsUrl")
      val con = new WebSocket(wsUrl)
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
    println("wsOnMessage:" + ev.data.toString)
  }


  private def wsOnError(ev: Event): Unit = {
    println("wsOnError:" + ev.`type`)
  }


  private def wsOnClose(ev: CloseEvent): Unit = {
    println(s"wsOnClose: [${ev.code}, ${ev.reason}, ${ev.wasClean}]" )
  }


  private def sendMessage(): Unit = {
    val msg = messageInput.map(_.value).getOrElse("NULL")
    messageBoard.update { current =>
      current + "\n" +
      s"$username: " + msg
    }
    boardElement.foreach { b =>
      println("move down...")
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

