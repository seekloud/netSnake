package com.neo.sk.hiStream.snake

import java.awt.event.KeyEvent

import org.slf4j.LoggerFactory

import scala.util.Random


/**
  * User: Taoz
  * Date: 9/1/2016
  * Time: 5:34 PM
  */
class Grid(boundary: Point) {


  val log = LoggerFactory.getLogger(this.getClass)

  val defaultLength = 5
  val appleNum = 6
  val appleLife = 50

  private[this] var grid = Map[Point, Spot]()
  private[this] var snakes = Map.empty[Long, SnakeData]
  private[this] var actionMap = Map.empty[Long, Int]
  private[this] var waitingJoin = Map.empty[Long, String]


  def addSnake(id: Long, name: String) = waitingJoin += (id -> name)

  private[this] def genWaitingSnake() = {
    waitingJoin.filterNot(kv => snakes.contains(kv._1)).foreach { case (id, name) =>
      val header = randomEmptyPoint()
      grid += header -> Body(id, defaultLength)
      snakes += id -> SnakeData(id, name, header)
    }
    waitingJoin = Map.empty[Long, String]
  }


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
    genWaitingSnake()
  }

  private[this] def updateSpots() = {
    log.debug(s"grid: ${grid.mkString(";")}")
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


    if(appleCount < snakes.size * 2 + appleNum ) {
      val p = randomEmptyPoint()
      val score = random.nextDouble() match {
        case x if x > 0.95 => 10
        case x if x > 0.8 => 5
        case x => 1
      }
      val apple = Apple(score, appleLife)
      grid += (p -> apple)
    }
  }

  val random = new Random(System.nanoTime())

  def randomEmptyPoint(): Point = {
    var p = Point(random.nextInt(boundary.x), random.nextInt(boundary.y))
    while (grid.contains(p)) {
      p = Point(random.nextInt(boundary.x), random.nextInt(boundary.y))
    }
    p
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

      val newHeader = ((snake.header + newDirection) + boundary) % boundary

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


    //if two (or more) headers go to the same point,
    val snakesInDanger = updatedSnakes.groupBy(_.header).filter(_._2.size > 1)
    val deadSnakes = snakesInDanger.values.flatMap(_.toSeq.sortBy(_.length).tail).map(_.id).toSet


    val newSnakes = updatedSnakes.filterNot(s => deadSnakes.contains(s.id))
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
