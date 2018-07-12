package com.neo.sk.hiStream.chat

import java.nio.ByteBuffer

/**
  * User: Taoz
  * Date: 7/12/2018
  * Time: 11:23 AM
  */
class MiddleDataInJvm extends MiddleData {

  private var data: ByteBuffer = _
  //  private var index = 0

  def init(buffer: ByteBuffer): Unit = {
    data = buffer
    data.reset()
  }

  override def init(size: Int): Unit = {
    data = ByteBuffer.allocate(size)
  }

  override def reset(): Unit = {
    data.reset()
  }

  override def putString(s: String): Unit = {
    val bytes = s.getBytes("utf-8")
    val len = bytes.length
    putInt(len)
    data.put(bytes)
  }

  override def putByte(b: Byte): Unit = data.put(b)

  override def putInt(i: Int): Unit = data.putInt(i)

  override def putFloat(f: Float): Unit = data.putFloat(f)

  override def putMiddleData(d: MiddleData): Unit = {
    throw new NotImplementedError()
  }

  override def getString(): String = {
    val len = getInt()
    val bytes = new Array[Byte](len)
    data.get(bytes)
    new String(bytes, "utf-8")
  }

  override def getByte(): Byte = data.get()


  override def getInt(): Int = data.getInt()

  override def getFloat(): Float = data.getFloat()

  override def getMiddleData(): MiddleData = {
    throw new NotImplementedError()
  }

  def result: Array[Byte] = data.array()
}
