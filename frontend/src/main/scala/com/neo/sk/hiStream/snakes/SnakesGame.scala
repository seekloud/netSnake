package com.neo.sk.hiStream.snakes

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.ext.Color
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

case class Header(life: Int) extends Spot


case class SnakesGame(bounds: Point, resetGame: () => Unit) extends Game {

  val canvasUnit = 10

  val boundary = Point(bounds.x / canvasUnit, bounds.y / canvasUnit)
  var frameCount = 0l


  val snake = new Snake(boundary.x / 2, boundary.y / 2)
  var grid = Map[Point, Spot]()

  override def update(keysDown: List[Int]): Unit = {
    frameCount += 1

    if (frameCount % 1 == 0) {


      println(s" +++ snake feel key: ${keysDown.lastOption}")
      val newDirection = keysDown.lastOption match {
        case Some(KeyCode.Left) => Point(-1, 0)
        case Some(KeyCode.Right) => Point(1, 0)
        case Some(KeyCode.Up) => Point(0, -1)
        case Some(KeyCode.Down) => Point(0, 1)
        case _ => snake.direction
      }

      if (newDirection + snake.direction != Point(0, 0)) {
        snake.direction = newDirection
      }

      val header = ((snake.header + snake.direction) + boundary) % boundary

      grid = grid.filterNot { case (p, spot) =>
        spot match {
          case Body(life) if life < 0 => true
          case _ => false
        }
      }.map {
        case (p, Header(life)) => (p, Body(life - 1))
        case (p, Body(life)) => (p, Body(life - 1))
        case x => x
      }

      grid.get(header) match {
        case Some(x: Body) =>
          println(" --------------- hit something..........")
          resetGame()
        case _ =>
          snake.header = header
          println(s" +++ header: ${snake.header}")
          println(s" +++ bodys: ${grid.mkString(", ")}")
          grid += snake.header -> Header(snake.length)
      }



    }

  }


  override def draw(ctx: CanvasRenderingContext2D): Unit = {
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, boundary.x * canvasUnit, boundary.y * canvasUnit)

    //println("---------------  draw   ------------------")
    ctx.fillStyle = Color(200, 200, 200).toString()
    grid.foreach { case (p@Point(x, y), spot) =>
      spot match {
        case Body(life) =>
          //println(s"draw body at $p body[$life]")
          ctx.fillRect(x * canvasUnit + 1, y * canvasUnit + 1, canvasUnit - 1, canvasUnit - 1)
        case Header(life) =>
          ctx.save()
          ctx.fillStyle = Color.Green.toString()
          ctx.fillRect(x * canvasUnit + 1, y * canvasUnit + 1, canvasUnit - 1, canvasUnit - 1)
          ctx.restore()


      }
    }
  }


  def drawTest1(ctx: CanvasRenderingContext2D) = {
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, 1200, 800)

    val x = 10
    val y = 10
    val w = 10
    val h = 10

    ctx.fillStyle = Color(200, 200, 200).toString()
    ctx.fillRect(x, y, w, h)
  }

  var count = 1

  def drawTest2(ctx: CanvasRenderingContext2D) = {
    println(s"drawTest2 count=$count")
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, 1200, 800)

    val x = 10
    val y = 10
    val w = 10
    val h = 10

    ctx.fillStyle = Color(200, 200, 200).toString()
    ctx.fillRect(x * count, y * count, w, h)
    count += 1
  }

}


class Snake(x: Int, y: Int) {

  var length = 50
  var direction = Point(1, 0)
  var header = Point(x, y)

}

