package com.neo.sk.hiStream.snake.scalajs

import com.neo.sk.hiStream.snake._
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


  val bounds = Point(Boundary.w, Boundary.h)
  val canvasUnit = 10
  val canvasBoundary = bounds * canvasUnit

  val watchKeys = Set(
    KeyCode.Space, KeyCode.Left, KeyCode.Up, KeyCode.Right, KeyCode.Down, KeyCode.Space
  )

  object MyColors {
    val myHeader = "#FF0000"
    val myBody = "#FFFFFF"
    val otherHeader = Color.Blue.toString()
    val otherBody = "#696969"
  }

  private[this] val nameField = dom.document.getElementById("name").asInstanceOf[HTMLInputElement]
  private[this] val joinButton = dom.document.getElementById("join").asInstanceOf[HTMLButtonElement]
  private[this] val canvas = dom.document.getElementById("GameView").asInstanceOf[Canvas]
  private[this] val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {

    drawGameOff()
    canvas.width = canvasBoundary.x
    canvas.height = canvasBoundary.y

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
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, canvas.width, canvas.height)
  }

  def drawGameOff(): Unit = {
    ctx.fillStyle = Color.Blue.toString()
    ctx.fillRect(0, 0, canvas.width, canvas.height)
  }

  //TODO here
  def drawGrid(dataMsg: Protocol.GridDataMessage): Unit = {

    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, bounds.x * canvasUnit, bounds.y * canvasUnit)

    val uid = dataMsg.uid
    val data = dataMsg.data
    val snakes = data.snakes
    val bodys = data.bodyDetails
    val apples = data.appleDetails

    ctx.fillStyle = MyColors.otherBody
    bodys.foreach { case BodyDetail(id, life, x, y) =>
      //println(s"draw body at $p body[$life]")
      if (id == uid) {
        ctx.save()
        ctx.fillStyle = MyColors.myBody
        ctx.fillRect(x * canvasUnit + 1, y * canvasUnit + 1, canvasUnit - 1, canvasUnit - 1)
        ctx.restore()
      } else {
        ctx.fillRect(x * canvasUnit + 1, y * canvasUnit + 1, canvasUnit - 1, canvasUnit - 1)
      }
    }

    apples.foreach { case AppleDetail(score, life, x, y) =>
      ctx.fillStyle = score match {
        case 10 => Color.Yellow.toString()
        case 5 => Color.Blue.toString()
        case _ => Color.Red.toString()
      }
      ctx.fillRect(x * canvasUnit + 1, y * canvasUnit + 1, canvasUnit - 1, canvasUnit - 1)
    }

    ctx.fillStyle = MyColors.otherHeader
    snakes.foreach { snake =>
      val id = snake.id
      val x = snake.header.x
      val y = snake.header.y
      if (id == uid) {
        ctx.save()
        ctx.fillStyle = MyColors.myHeader
        ctx.fillRect(x * canvasUnit + 2, y * canvasUnit + 2, canvasUnit - 4, canvasUnit - 4)
        ctx.restore()
      } else {
        ctx.fillRect(x * canvasUnit + 2, y * canvasUnit + 2, canvasUnit - 4, canvasUnit - 4)
      }
    }


    ctx.fillStyle = "rgb(250, 250, 250)"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    snakes.groupBy(_.kill).toList.sortBy(_._1).reverse.headOption.foreach { case (kill, s) =>
      ctx.font = "12px Helvetica"
      ctx.fillText(s"Top Killer: ${s.map(_.name).mkString(", ")}, kill=$kill, length=${s.length}", 10, 10)
    }


    snakes.find(_.id == uid) match {
      case Some(mySnake) =>
        ctx.font = "12px Helvetica"
        ctx.fillText("your id     : " + mySnake.id, 10, 40)
        ctx.fillText("your kill   : " + mySnake.kill, 10, 40)
        ctx.fillText("your length : " + mySnake.length, 10, 54)
      case None =>
        ctx.font = "36px Helvetica"
        ctx.fillText("Ops, Press Space Key To Restart!", 150, 180)
    }


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
        case data: Protocol.GridDataMessage =>
          //writeToArea(s"data got: $data")
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
