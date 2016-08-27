package com.neo.sk.hiStream.snakes

import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode

import scala.collection.mutable

/**
  * User: Taoz
  * Date: 8/27/2016
  * Time: 8:50 PM
  */

trait Spot

case class Body(position: Point, life: Int) extends Spot

class SnakesGame() {

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


  val grid = new mutable.HashMap[Point, Spot]()
  val snake = new Snake()


  def update() = {
    frameCount += 1

    if (frameCount % 3 == 0) {
      val header = snake.header + snake.direction

      grid.get(header) match {
        case Some(Body(_, _)) => reset()
        case None =>
          grid(header) = Body(header, snake.length)
      }
      snake.header = header

      val newDirection =
        if(keysDown(KeyCode.Left)) Point(-1, 0)
        else if (keysDown(KeyCode.Right)) Point(1, 0)
        else if (keysDown(KeyCode.Up)) Point(0, -1)
        else if (keysDown(KeyCode.Down)) Point(0, 1)
        else snake.direction

      if (newDirection + snake.direction != Point(0, 0)) {
        snake.direction = newDirection
      }

      val tobeRemove = grid.filter { case (p, spot) =>
        spot match {
          case Body(_, life) if life < 0 => true
          case _ => false
        }
      }.keySet
      grid --= tobeRemove

    }

  }


  def draw() = {

  }

  def reset() = {

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
