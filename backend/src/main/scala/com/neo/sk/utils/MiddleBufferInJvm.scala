package com.neo.sk.utils

import java.nio.ByteBuffer

import com.neo.sk.hiStream.utils.MiddleBuffer

/**
  * User: Taoz
  * Date: 7/12/2018
  * Time: 11:23 AM
  */
class MiddleBufferInJvm private() extends MiddleBuffer {

  private[this] var data: ByteBuffer = _

  def this(array: Array[Byte]) {
    this()
    data = ByteBuffer.wrap(array)
  }

  def this(buffer: ByteBuffer) {
    this()
    data = buffer
  }

  def this(size: Int) = {
    this()
    data = ByteBuffer.allocate(size)
  }

  override def clear(): Unit = {
    data.clear()
  }

  override def putByte(b: Byte): MiddleBufferInJvm = {
    data.put(b)
    this
  }

  override def putInt(i: Int): MiddleBufferInJvm = {
    data.putInt(i)
    this
  }

  override def putFloat(f: Float): MiddleBufferInJvm = {
    data.putFloat(f)
    this
  }

  override def getByte(): Byte = {
    val rst = data.get()
//    println(s"getByte: int=[$rst]")
    rst
  }


  override def getInt(): Int = {
    val rst = data.getInt()
//    println(s"getInt: int=[$rst]")
    rst
  }

  /*
    def getInt(i: Int) : Int = {
      data.getInt(i)
    }
  */

  override def getFloat(): Float = data.getFloat()


  override def result(): Array[Byte] = {
    val length = data.position()
//    println(s"result length: $length")
    data.flip()
    val rst = new Array[Byte](data.limit())
    var c = 0
    while (data.hasRemaining) {
      rst(c) = data.get()
      c += 1
    }
    rst
  }
}
