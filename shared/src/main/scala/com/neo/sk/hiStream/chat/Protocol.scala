package com.neo.sk.hiStream.chat

/**
  * User: Taoz
  * Date: 7/12/2018
  * Time: 9:13 AM
  */
object Protocol {


  case class TestMessage(id: Int, data: String, ls: Array[Float])


  object TestMessage {
    def decode(data: MiddleData): TestMessage = {
      val id = data.getInt()
      println(s"TestMessage id=$id")
      val d = data.getString()
      println(s"TestMessage data=$d")
      val len = data.getInt()
      println(s"TestMessage len=$len")
      val ls = new Array[Float](len)
      for(i <- 0 until len){
        ls(i) = data.getFloat()
      }
      println(s"TestMessage ls=${ls.mkString(",")}")
      TestMessage(id, d, ls)
    }

    def encode(target: TestMessage, container: MiddleData): Unit = {
      container.init(64)
      container.putInt(target.id)
      container.putString(target.data)
      container.putInt(target.ls.length)
      target.ls.foreach(container.putFloat)
    }
  }


}

trait Msg


