
package com.neo.sk.hiStream.chat

import scala.reflect.ClassTag

/**
  * User: Taoz
  * Date: 7/12/2018
  * Time: 1:34 PM
  */
trait MiddleBuffer {

  def clear(): Unit

  def result(): Any

  def putString(s: String): Unit = {
    val bytes = s.getBytes("utf-8")
    putInt(bytes.length)
    bytes.foreach { b => putByte(b) }
  }

  def putByte(b: Byte): Unit

  def putInt(i: Int): Unit

  def putFloat(f: Float): Unit

  def putIntArray(ls: Array[Int]): Unit = putXArray(putInt)(ls)

  def putFloatArray(ls: Array[Float]): Unit = putXArray(putFloat)(ls)

  def putStringArray(ls: Array[String]): Unit = putXArray(putString)(ls)

  def putXArray[A](putXFunc: A => Unit )(ls: Array[A]): Unit = {
    putInt(ls.length)
    var i = 0
    while (i < ls.length) {
      putXFunc(ls(i))
      i += 1
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

  def getIntArray(): Array[Int] = getXArray(getInt )

  def getFloatArray(): Array[Float] = getXArray(getFloat)

  def getStringArray(): Array[String] = getXArray(getString)

  private def getXArray[T](getFunc: () => T)(implicit m: ClassTag[T]): Array[T] = {
    val len = getInt()
    val ls = new Array[T](len)
    var c = 0
    while (c < len) {
      ls(c) = getFunc()
      c += 1
    }
    ls
  }

}

