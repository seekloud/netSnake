package com.neo.sk.hiStream.snake.scalajs

import com.neo.sk.hiStream.snake.{Protocol, scalajs}
import org.scalajs.dom
import org.scalajs.dom.ext.{Color, KeyCode}
import org.scalajs.dom.html.{Document => _, _}
import org.scalajs.dom.raw._
import upickle.default._

import scala.scalajs.js

/**
  * User: Taoz
  * Date: 9/1/2016
  * Time: 12:45 PM
  */
object NetGameHolder extends js.JSApp {


  val watchKeys = Set(
    KeyCode.Space, KeyCode.Left, KeyCode.Up, KeyCode.Right, KeyCode.Down
  )

  private[this] val nameField = dom.document.getElementById("name").asInstanceOf[HTMLInputElement]
  private[this] val joinButton = dom.document.getElementById("join").asInstanceOf[HTMLButtonElement]
  private[this] val canvas = dom.document.getElementById("GameView").asInstanceOf[Canvas]
  private[this] val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {

    drawGameOff()

    joinButton.onclick = { (event: MouseEvent) =>
      joinGame(nameField.value)
      event.preventDefault()
    }
    nameField.focus()
    nameField.onkeypress = { (event: KeyboardEvent) =>
      if (event.keyCode == 13) {
        joinButton.click()
        event.preventDefault()
      }
    }
  }

  def drawGameOn(): Unit = {
    ctx.fillStyle = Color.Green.toString()
    ctx.fillRect(0, 0, 400, 50)
  }

  def drawGameOff(): Unit = {
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, 400, 50)
  }

  //TODO here
  def drawGrid(data: Protocol.GridDataMessage): Unit = {

  }


  def joinGame(name: String): Unit = {
    joinButton.disabled = true
    val playground = dom.document.getElementById("playground")
    playground.innerHTML = s"Trying to join game as '$name'..."
    val gameStream = new WebSocket(getWebSocketUri(dom.document, name))
    gameStream.onopen = { (event0: Event) =>
      drawGameOn()
      playground.insertBefore(p("Game connection was successful!"), playground.firstChild)
      canvas.focus()
      canvas.onkeydown = {
        (e: dom.KeyboardEvent) => {
          println(s"keydown: ${e.keyCode}")
          if (watchKeys.contains(e.keyCode)) {
            println(s"got key: [${e.keyCode}]")
            gameStream.send(e.keyCode.toString)
            //TODO send key
            e.preventDefault()
          }
        }
      }
      event0
    }

    gameStream.onerror = { (event: ErrorEvent) =>
      drawGameOff()
      playground.insertBefore(p(s"Failed: code: ${event.colno}"), playground.firstChild)
      joinButton.disabled = false
      nameField.focus()
    }

    gameStream.onmessage = { (event: MessageEvent) =>
      val wsMsg = read[Protocol.GameMessage](event.data.toString)
      wsMsg match {
        case Protocol.TextMsg(message) => writeToArea(s"MESSAGE: $message")
        case Protocol.NewSnakeJoined(id, user) => writeToArea(s"$user joined!")
        case Protocol.SnakeLeft(id, user) => writeToArea(s"$user left!")
        case data: Protocol.GridDataMessage => writeToArea(s"data got: $data")
          drawGrid(data)
      }
    }



    gameStream.onclose = { (event: Event) =>
      drawGameOff()
      playground.insertBefore(p("Connection to game lost. You can try to rejoin manually."), playground.firstChild)
      joinButton.disabled = false
      nameField.focus()
    }

    def writeToArea(text: String): Unit =
      playground.insertBefore(p(text), playground.firstChild)
  }

  def getWebSocketUri(document: Document, nameOfChatParticipant: String): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host}/hiStream/netSnake/join?name=$nameOfChatParticipant"
  }

  def p(msg: String) = {
    val paragraph = dom.document.createElement("p")
    paragraph.innerHTML = msg
    paragraph
  }


}
