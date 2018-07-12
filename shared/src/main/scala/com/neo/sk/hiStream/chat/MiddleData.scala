package com.neo.sk.hiStream.chat

/**
  * User: Taoz
  * Date: 7/12/2018
  * Time: 1:34 PM
  */
trait MiddleData {

  def init(size: Int): Unit

  def reset(): Unit

  def result(): Any

  def putString(s: String): Unit = {
    val bytes = s.getBytes("utf-8")
    putInt(bytes.length)
    bytes.foreach { b => putByte(b) }
  }

  def putByte(b: Byte): Unit

  def putInt(i: Int): Unit

  def putIntArray(ls: Array[Int]): Unit = {
    putInt(ls.length)
    val i = 0
    while (i < ls.length){
      putInt(ls(i))
    }
  }

  def putFloat(f: Float): Unit

  def putFloatArray(ls: Array[Float]): Unit = {
    putInt(ls.length)
    val i = 0
    while (i < ls.length){
      putFloat(ls(i))
    }
  }

  def getString(): String = {
    val len = getInt()
    val bytes = new Array[Byte](len)
    for (i <- 0 until len) {
      bytes(i) = getByte()
    }
    new String(bytes, "utf-8")
  }

  def getByte(): Byte

  def getInt(): Int

  def getFloat(): Float

}

