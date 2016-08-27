package com.neo.sk.hiStream.snakes

import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html._
import scala.scalajs.js
import scala.collection.mutable

/**
  * User: Taoz
  * Date: 8/27/2016
  * Time: 8:50 PM
  */

sealed trait Spot

case class Body(life: Int) extends Spot

object SnakesGame extends js.JSApp {

  def main(): Unit = {
    val snakeGame = new SnakesGame("snake")
    dom.window.setInterval(() => snakeGame.gameLoop(), 1000)
  }

}


class SnakesGame(canvasName: String) {

  private[this] val canvas = dom.document.getElementById(canvasName).asInstanceOf[Canvas]
  private[this] val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  canvas.width = 1200
  canvas.height = 600

  val groundWidth = 120
  val groundHeight = 80
  var frameCount = 0l


  // Handle keyboard controls
  val keysDown = new mutable.LinkedHashSet[Int]()

  dom.window.addEventListener("keydown", listener = (e: dom.KeyboardEvent) => {
    keysDown += e.keyCode
  }, useCapture = false)

  dom.window.addEventListener("keyup", (e: dom.KeyboardEvent) => {
    keysDown -= e.keyCode
  }, false)


  var grid = Map[Point, Spot]()
  var snake = new Snake()

  def gameLoop() = {
    draw()
    update()
  }


  def update() = {
    frameCount += 1

    if (frameCount % 2 == 0) {

      println(s"header: ${snake.header}")
      println(s"bodys: ${grid.mkString(", ")}")
      val header = snake.header + snake.direction

      grid.get(header) match {
        case Some(Body(_)) => reset()
        case None =>
          grid += header -> Body(snake.length)
      }
      snake.header = header

      val newDirection =
        if (keysDown(KeyCode.Left)) Point(-1, 0)
        else if (keysDown(KeyCode.Right)) Point(1, 0)
        else if (keysDown(KeyCode.Up)) Point(0, -1)
        else if (keysDown(KeyCode.Down)) Point(0, 1)
        else snake.direction

      if (newDirection + snake.direction != Point(0, 0)) {
        snake.direction = newDirection
      }

      grid = grid.filterNot { case (p, spot) =>
        spot match {
          case Body(life) if life < 0 => true
          case _ => false
        }
      }.map {
        case (p, Body(life)) => (p, Body(life - 1))
        case x => x
      }
    }

  }


  def draw() = {
    ctx.fillStyle = "rgb(0, 0, 0)"
    ctx.fillRect(0, 0, 1200, 800)

    println("---------------  draw   ------------------")
    grid.foreach { case (p@Point(x, y), spot) =>
      spot match {
        case Body(life) =>
          println(s"draw body at $p body[$life]")
          ctx.fillStyle = "rgb(200, 200, 200)"
          ctx.fillRect(x * 10, y * 10, 10, 10)
      }
    }
  }

  def reset() = {
    snake = new Snake
  }

}


class Snake() {

  var length = 5
  var direction = Point(1, 0)
  var header = Point(60, 40)

}

case class Point(x: Int, y: Int) {
  def +(other: Point) = Point(x + other.x, y + other.y)

  def -(other: Point) = Point(x - other.x, y - other.y)

}
