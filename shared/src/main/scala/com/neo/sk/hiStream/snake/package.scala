package com.neo.sk.hiStream



/**
  * User: Taoz
  * Date: 8/29/2016
  * Time: 9:48 PM
  */
package object snake {

  sealed trait Spot
  case class Body(id: Long, life: Int) extends Spot
  case class Header(id: Long, life: Int) extends Spot
  case class Apple(score: Int, life: Int) extends Spot


  case class BodyDetail(id: Long, life: Int, x: Int, y: Int)
  case class AppleDetail(score: Long, life: Int, x: Int, y: Int)

  case class GridDataSync(
    snakes: List[SnakeData],
    bodyDetails: List[BodyDetail],
    appleDetails: List[AppleDetail]
  )

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





}
