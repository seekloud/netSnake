package com.neo.sk.hiStream.snake

import java.awt.event.KeyEvent

import org.slf4j.LoggerFactory


/**
  * User: Taoz
  * Date: 9/1/2016
  * Time: 5:34 PM
  */
class Grid(boundary: Point) {


  val log = LoggerFactory.getLogger(this.getClass)

  private[this] var grid = Map[Point, Spot]()
  private[this] var snakes = Map.empty[Long, SnakeData]
  private[this] var actionMap = Map.empty[Long, Int]


  def addSnake(id: Long, name: String) = snakes += id -> SnakeData(id, name)


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

  private[this] def updateSpots() = {
    println(s"grid: ${grid.mkString(";")}")
    grid = grid.filter { case (p, spot) =>
      spot match {
        case Body(id, life) if life >= 0 && snakes.contains(id) => true
        case Apple(_, life) if life >= 0 => true
        case Header(id, _) if snakes.contains(id) => true
        case _ => false
      }
    }.map {
      case (p, Header(id, life)) => (p, Body(id, life - 1))
      case (p, b@Body(_, life)) => (p, b.copy(life = life - 1))
      case (p, a@Apple(_, life)) => (p, a.copy(life = life - 1))
      case x => x
    }
  }


  private[this] def updateSnakes() = {

    def updateASnake(snake: SnakeData) = {
      val keyCode = actionMap.get(snake.id)
      log.debug(s" +++ snake[${snake.id}] feel key: $keyCode")
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
      val newHeader = ((snake.header + snake.direction) + boundary) % boundary

      grid.get(newHeader) match {
        case Some(x: Body) =>
          log.info(s"snake[${snake.id}] hit wall.")
          None
        case Some(Apple(score, _)) =>
          val len = snake.length + score
          grid -= newHeader
          Some(snake.copy(header = newHeader, direction = newDirection, length = len))
        case _ =>
          Some(snake.copy(header = newHeader, direction = newDirection))
      }
    }

    val updatedSnakes = snakes.values.flatMap(updateASnake)

    actionMap = Map.empty

    //if two (or more) headers go to the same point,
    val snakesInDanger = updatedSnakes.groupBy(_.header).filter(_._2.size > 1)
    val deadSnakes = snakesInDanger.values.flatMap(_.toSeq.sortBy(_.id).reverse.tail).map(_.id).toSet


    val newSnakes = updatedSnakes.filterNot(s => deadSnakes.contains(s.id))
    grid ++= newSnakes.map(s => s.header -> Header(s.id, s.length))
    snakes = newSnakes.map(s => (s.id, s)).toMap

  }


  def updateAndGetGridData() = {
    update()
    getGridData
  }

  def getGridData = {

    var bodyDetails: List[BodyDetail] = Nil
    var appleDetails: List[AppleDetail] = Nil

    grid.foreach{
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
