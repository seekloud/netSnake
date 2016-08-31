package com.neo.sk.hiStream

import sun.security.util.Length


/**
  * User: Taoz
  * Date: 8/29/2016
  * Time: 9:48 PM
  */
package object snake {

  sealed trait Spot
  case class Body(life: Int) extends Spot
  case class Header(life: Int) extends Spot
  case class Apple(score: Int, life: Int) extends Spot


  case class Point(x: Int, y: Int) {
    def +(other: Point) = Point(x + other.x, y + other.y)

    def -(other: Point) = Point(x - other.x, y - other.y)

    def %(other: Point) = Point(x % other.x, y % other.y)
  }


  class Snake(x: Int, y: Int, len: Int = 5, d: Point = Point(1, 0)) {
    var length = len
    var direction = d
    var header = Point(x, y)
  }

  case class SnakeData(
    id: Long,
    name: String,
    header: Point = Point(20, 20),
    direction: Point = Point(1, 0),
    length: Int = 4)


  sealed trait GameOutPut

  case class GridDataSync(
    snakes: Seq[SnakeData],
    bodyPositions:Seq[Point],
    bodyLives: Seq[Int],
    applePositions: Seq[Point],
    apples:Seq[Apple]
  ) extends GameOutPut

  case class NewSnakeJoined(id: String, name: String)
  case class SnakeLeft(id: String, name: String)

}
