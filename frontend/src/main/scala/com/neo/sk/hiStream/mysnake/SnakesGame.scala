package com.neo.sk.hiStream.mysnake

import com.neo.sk.hiStream.snake.{Body, _}
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.ext.{Color, KeyCode}
import org.scalajs.dom.html._

import scala.util.Random

/**
  * User: Taoz
  * Date: 8/27/2016
  * Time: 8:50 PM
  */
case class SnakesGame(bounds: Point, resetGame: () => Unit) extends Game {

  val random = new Random(System.nanoTime())
  val canvasUnit = 10

  val boundary = Point(bounds.x / canvasUnit, bounds.y / canvasUnit)
  var frameCount = 0l

  val appleLife = 50
  val appleSum = 6

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
          case Body(_, life) if life < 0 => true
          case Apple(_, life) if life < 0 => true
          case _ => false
        }
      }.map {
        case (p, Header(id, life)) => (p, Body(id, life - 1))
        case (p, b@Body(_, life)) => (p, b.copy(life - 1))
        case (p, a@Apple(_, life)) => (p, a.copy(life = life - 1))
        case x => x
      }

      grid.get(header) match {
        case Some(x: Body) =>
          println(" --------------- hit something..........")
          resetGame()
        case Some(Apple(score, _)) =>
          snake.length += score
          grid -= header
        case _ => //do nothing.
      }

      snake.header = header
      println(s" +++ header: ${snake.header}")
      println(s" +++ bodys: ${grid.mkString(", ")}")
      grid += snake.header -> Header(1l, snake.length)
      grid = seedApple(grid)

    }



    def seedApple(grid : collection.immutable.Map[Point, Spot] ) = {
      val appleCount = grid.count{ case (_, a)  => a.isInstanceOf[Apple]}
      if(appleCount < appleSum) {
        var p = Point(random.nextInt(boundary.x), random.nextInt(boundary.y))
        while (grid.contains(p)){
          p = Point(random.nextInt(boundary.x), random.nextInt(boundary.y))
        }
        val score = random.nextDouble() match {
          case x if x > 0.95 => 10
          case x if x > 0.8 => 3
          case x => 1
        }
        val apple = Apple(score, appleLife)
        grid + (p -> apple)
      } else grid
    }

  }


  override def draw(ctx: CanvasRenderingContext2D): Unit = {
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, boundary.x * canvasUnit, boundary.y * canvasUnit)


    //println("---------------  draw   ------------------")
    ctx.fillStyle = Color(200, 200, 200).toString()
    grid.foreach { case (p@Point(x, y), spot) =>
      spot match {
        case Body(_, life) =>
          //println(s"draw body at $p body[$life]")
          ctx.fillRect(x * canvasUnit + 1, y * canvasUnit + 1, canvasUnit - 1, canvasUnit - 1)
        case Header(_, life) =>
          ctx.save()
          ctx.fillStyle = Color.Green.toString()
          ctx.fillRect(x * canvasUnit + 1, y * canvasUnit + 1, canvasUnit - 1, canvasUnit - 1)
          ctx.restore()
        case Apple(score, life) =>
          ctx.save()
          ctx.fillStyle = score match {
            case 10 => Color.Yellow.toString()
            case 3 => Color.Blue.toString()
            case _ => Color.Red.toString()
          }
          ctx.fillRect(x * canvasUnit + 1, y * canvasUnit + 1, canvasUnit - 1, canvasUnit - 1)
          ctx.restore()
      }
    }

    // Score
    ctx.fillStyle = "rgb(250, 250, 250)"
    ctx.font = "24px Helvetica"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    ctx.fillText("snake length: " + snake.length, 10, 10)

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



