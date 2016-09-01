package com.neo.sk.hiStream.snake

/**
  * User: Taoz
  * Date: 8/29/2016
  * Time: 9:40 PM
  */
object Protocol {
  sealed trait GameMessage

  case class GridDataSync(
    uid: Long,
    snakes: Seq[SnakeData],
    bodyPositions:Seq[Point],
    bodyLives: Seq[Int],
    applePositions: Seq[Point],
    apples:Seq[Apple]
  ) extends GameMessage

  case class TextMsg(
    msg: String
  ) extends GameMessage

  case class NewSnakeJoined(id: Long, name: String) extends GameMessage
  case class SnakeLeft(id: Long, name: String) extends GameMessage

}
