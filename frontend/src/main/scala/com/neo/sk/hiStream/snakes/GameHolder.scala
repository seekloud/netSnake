package com.neo.sk.hiStream.snakes

import com.neo.sk.hiStream.snake.Point
import org.scalajs.dom
import org.scalajs.dom.ext.{Color, KeyCode}
import org.scalajs.dom.html._

import scala.collection.mutable
import scala.scalajs.js

/**
  * User: Taoz
  * Date: 8/28/2016
  * Time: 11:10 AM
  */

object GameHolder extends js.JSApp {
  def main(): Unit = {

    val holder = new GameHolder("snake", SnakesGame)

    dom.window.setInterval(() => holder.gameLoop(), 100)
    //dom.window.setInterval(() => snakeGame.drawTest2(), 3000)
    //snakeGame.draw()
  }
}

class GameHolder(canvasName: String, gameMaker: (Point, () => Unit) => Game) {

  val watchKeys = Set(
    KeyCode.Space, KeyCode.Left, KeyCode.Up, KeyCode.Right, KeyCode.Down
  )

  private[this] val canvas = dom.document.getElementById(canvasName).asInstanceOf[Canvas]


  canvas.width = 1200
  canvas.height = 500
  canvas.tabIndex = 1


  val boundary = Point(canvas.width, canvas.height)

  val keysDown = new mutable.LinkedHashSet[Int]()
  val keysUp = new mutable.HashSet[Int]()

  // Handle keyboard controls
  /*  dom.window.addEventListener("keydown", (e: dom.KeyboardEvent) => {
      if (watchKeys.contains(e.keyCode)) {
        println(s"keydown: ${e.keyCode}")
        keysDown += e.keyCode
        e.preventDefault()
      }
    }, useCapture = false)

    dom.window.addEventListener("keyup", (e: dom.KeyboardEvent) => {
      if (watchKeys.contains(e.keyCode)) {
        println(s"keyup: ${e.keyCode}")
        keysUp += e.keyCode
        //keysDown -= e.keyCode
        e.preventDefault()
      }
    }, false)*/

  canvas.onkeydown = {
    (e: dom.KeyboardEvent) => {
      println(s"keydown: ${e.keyCode}")
      if (watchKeys.contains(e.keyCode)) {
        keysDown += e.keyCode
        e.preventDefault()
      }
    }
  }

  canvas.onkeyup = {
    (e: dom.KeyboardEvent) => {
      println(s"keyup: ${e.keyCode}")
      if (watchKeys.contains(e.keyCode)) {
        keysUp += e.keyCode
        //keysDown -= e.keyCode
        e.preventDefault()
      }
    }
  }

  private[this] val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  var game = gameMaker(boundary, () => resetGame())


  def gameLoop() = {
    game.update(keysDown.toList)
    keysDown --= keysUp
    game.draw(ctx)
  }

  def resetGame(): Unit = {
    println(" --------------------- resetGame --------------")
    game = gameMaker(boundary, () => resetGame())
  }


}


trait Game {

  def update(keysDown: List[Int]): Unit

  def draw(ctx: dom.CanvasRenderingContext2D): Unit

}



