package com.neo.sk.hiStream.front.chat

import com.neo.sk.hiStream.front.utils.Component
import org.scalajs.dom

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import mhtml._
import org.scalajs.dom.html.TextArea
import org.scalajs.dom.raw.Event

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
  private var messageInput = ""
  private var boardElement: Option[TextArea] = None

  private val messageBoard: Var[String] = Var("")

  private def joinRoom(name: String): Unit = {
    println(s"$name click join button.")
  }

  private def sendMessage(msg: String): Unit = {
    messageBoard.update { current =>
      current + "\n" +
      s"$username: " + msg
    }
    boardElement.foreach { b =>
      println("move down...")
      b.scrollTop = b.scrollHeight
    }
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


      <input placeholder="say something."
             style="width:300px"
             onchange={e: Event => messageInput = e.target.asInstanceOf[dom.html.Input].value}>
        {messageInput}
      </input>
      <button onclick={() => sendMessage(messageInput)}>send</button>
    </div>
  }


}
