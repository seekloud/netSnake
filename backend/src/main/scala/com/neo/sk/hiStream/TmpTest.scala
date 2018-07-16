package com.neo.sk.hiStream

import com.neo.sk.hiStream.snake.Protocol

/**
  * User: Taoz
  * Date: 6/28/2018
  * Time: 7:26 PM
  */
object TmpTest {


  import io.circe._
  import io.circe.parser._
  import io.circe.generic.auto._
  import io.circe.syntax._

  sealed trait TmpMsg
  case class Action(name: String, value: Float) extends TmpMsg
  case class MultiAction(name: String, a1: Action, a2: Action) extends TmpMsg

  def main(args: Array[String]): Unit = {



    println("hello, world.")

    //val msg: Msg = Action("a1", 1.1f)
    val msg: TmpMsg = MultiAction("t1", Action("a1", 1.1f), Action("a2", 2.1f))
    val jsonStr = msg.asJson.noSpaces
    println("jsonStr:" + jsonStr)

    val m1 = decode[TmpMsg](jsonStr)
    println("m1:", m1)


  }

}





