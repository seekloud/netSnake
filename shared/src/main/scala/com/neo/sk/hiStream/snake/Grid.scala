package com.neo.sk.hiStream.snake

import java.awt.event.KeyEvent

import scala.util.Random


/**
  * User: Taoz
  * Date: 9/1/2016
  * Time: 5:34 PM
  */
trait Grid {

  val boundary: Point
  def debug(msg: String) : Unit
  def info(msg: String) : Unit

  val random = new Random(System.nanoTime())


  val defaultLength = 5
  val appleNum = 6
  val appleLife = 50
  val historyRankLength = 5

  var grid = Map[Point, Spot]()
  var snakes = Map.empty[Long, SnakeData]
  var actionMap = Map.empty[Long, Int]



  def removeSnake(id: Long): Option[SnakeData] = {
    val r = snakes.get(id)
    if (r.isDefined) {
      snakes -= id
    }
    r
  }


  def addAction(id: Long, keyCode: Int) = actionMap += id -> keyCode


  def update() = {
    updateSnakes()
    updateSpots()
  }

  def feedApple(appleCount: Int): Unit

  private[this] def updateSpots() = {
    debug(s"grid: ${grid.mkString(";")}")
    var appleCount = 0
    grid = grid.filter { case (p, spot) =>
      spot match {
        case Body(id, life) if life >= 0 && snakes.contains(id) => true
        case Apple(_, life) if life >= 0 => true
        //case Header(id, _) if snakes.contains(id) => true
        case _ => false
      }
    }.map {
      //case (p, Header(id, life)) => (p, Body(id, life - 1))
      case (p, b@Body(_, life)) => (p, b.copy(life = life - 1))
      case (p, a@Apple(_, life)) =>
        appleCount += 1
        (p, a.copy(life = life - 1))
      case x => x
    }

    feedApple(appleCount)
  }


  def randomEmptyPoint(): Point = {
    var p = Point(random.nextInt(boundary.x), random.nextInt(boundary.y))
    while (grid.contains(p)) {
      p = Point(random.nextInt(boundary.x), random.nextInt(boundary.y))
    }
    p
  }


  private[this] def updateSnakes() = {
    def updateASnake(snake: SnakeData): Either[Long, SnakeData] = {
      val keyCode = actionMap.get(snake.id)
      debug(s" +++ snake[${snake.id}] feel key: $keyCode")
      val newDirection = {
        val keyDirection = keyCode match {
          case Some(KeyEvent.VK_LEFT) => Point(-1, 0)
          case Some(KeyEvent.VK_RIGHT) => Point(1, 0)
          case Some(KeyEvent.VK_UP) => Point(0, -1)
          case Some(KeyEvent.VK_DOWN) => Point(0, 1)
          case _ => snake.direction
        }
        if (keyDirection + snake.direction != Point(0, 0)) {
          keyDirection
        } else {
          snake.direction
        }
      }

      val newHeader = ((snake.header + newDirection) + boundary) % boundary

      grid.get(newHeader) match {
        case Some(x: Body) =>
          debug(s"snake[${snake.id}] hit wall.")
          Left(x.id)
        case Some(Apple(score, _)) =>
          val len = snake.length + score
          grid -= newHeader
          Right(snake.copy(header = newHeader, direction = newDirection, length = len))
        case _ =>
          Right(snake.copy(header = newHeader, direction = newDirection))
      }
    }


    var mapKillCounter = Map.empty[Long, Int]
    var updatedSnakes = List.empty[SnakeData]

    snakes.values.map(updateASnake).foreach {
      case Right(s) => updatedSnakes ::= s
      case Left(killerId) =>
        mapKillCounter += killerId -> (mapKillCounter.getOrElse(killerId, 0) + 1)
    }


    //if two (or more) headers go to the same point,
    val snakesInDanger = updatedSnakes.groupBy(_.header).filter(_._2.size > 1).values

    val deadSnakes =
      snakesInDanger.flatMap { hits =>
        val sorted = hits.toSeq.sortBy(_.length)
        val winner = sorted.head
        val deads = sorted.tail
        mapKillCounter += winner.id -> (mapKillCounter.getOrElse(winner.id, 0) + deads.length)
        deads
      }.map(_.id).toSet


    val newSnakes = updatedSnakes.filterNot(s => deadSnakes.contains(s.id)).map { s =>
      mapKillCounter.get(s.id) match {
        case Some(k) => s.copy(kill = s.kill + k)
        case None => s
      }
    }

    grid ++= newSnakes.map(s => s.header -> Body(s.id, s.length))
    snakes = newSnakes.map(s => (s.id, s)).toMap

  }


  def updateAndGetGridData() = {
    update()
    getGridData
  }

  def getGridData = {

    var bodyDetails: List[BodyDetail] = Nil
    var appleDetails: List[AppleDetail] = Nil

    grid.foreach {
      case (p, Body(id, life)) => bodyDetails ::= BodyDetail(id, life, p.x, p.y)
      case (p, Apple(score, life)) => appleDetails ::= AppleDetail(score, life, p.x, p.y)
      case (p, Header(id, life)) => bodyDetails ::= BodyDetail(id, life, p.x, p.y)
    }
    GridDataSync(
      snakes.values.toList,
      bodyDetails,
      appleDetails
    )
  }


}
