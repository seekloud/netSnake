package com.neo.sk.hiStream.chat

import java.nio.ByteBuffer

/**
  * User: Taoz
  * Date: 7/12/2018
  * Time: 11:23 AM
  */
class MiddleDataInJvm extends MiddleData {

  private[this] var data: ByteBuffer = _
  //  private var index = 0

  def this(buffer: ByteBuffer) {
    this()
    data = buffer
  }

  override def init(size: Int): Unit = {
    data = ByteBuffer.allocate(size)
  }

  override def reset(): Unit = {
    data.rewind()
  }

  override def putByte(b: Byte): Unit = data.put(b)

  override def putInt(i: Int): Unit = data.putInt(i)

  override def putFloat(f: Float): Unit = data.putFloat(f)


  override def getByte(): Byte = data.get()


  override def getInt(): Int = data.getInt()

  override def getFloat(): Float = data.getFloat()


  override def result(): Array[Byte] = {
    val length = data.position()
    println(s"result length: $length")
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
