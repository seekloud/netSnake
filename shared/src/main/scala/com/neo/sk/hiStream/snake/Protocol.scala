package com.neo.sk.hiStream.snake

/**
  * User: Taoz
  * Date: 8/29/2016
  * Time: 9:40 PM
  */
class Protocol {
  sealed trait Message

  case class GridDataUpdate(id: Long, gridDataSync: GridDataSync)

}
