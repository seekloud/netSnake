package com.neo.sk.hiStream.chat

/**
  * User: Taoz
  * Date: 7/12/2018
  * Time: 9:13 AM
  */
object Protocol {


  case class TestMessage(id: Int, data: String, ls: Array[Float])


  object TestMessage {
    def decode(data: MiddleBuffer): TestMessage = {
      val id = data.getInt()
      val d = data.getString()
      val ls = data.getFloatArray()
      println(s"TestMessage ls=${ls.mkString(",")}")
      TestMessage(id, d, ls)
    }

    def encode(target: TestMessage, container: MiddleBuffer): Unit = {
      container.putInt(target.id)
      container.putString(target.data)
      container.putFloatArray(target.ls)
    }
  }


}

trait Msg


