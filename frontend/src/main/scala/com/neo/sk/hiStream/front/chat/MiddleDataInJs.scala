package com.neo.sk.hiStream.front.chat

import com.neo.sk.hiStream.chat.MiddleData

import scala.scalajs.js

/**
  * User: Taoz
  * Date: 7/12/2018
  * Time: 11:02 AM
  */
class MiddleDataInJs extends MiddleData {


  private var data: js.typedarray.DataView = _
  private var index: Int = -1


  override def reset(): Unit = {
    index = 0
  }

  def init(array: js.typedarray.ArrayBuffer): Unit = {
    val in = array
    data = new js.typedarray.DataView(in)
    index = 0
  }

  override def init(size: Int): Unit = {
    val in = new js.typedarray.ArrayBuffer(size)
    data = new js.typedarray.DataView(in)
    index = 0
  }

  override def putString(s: String): Unit = {
    val bytes = s.getBytes("utf-8")
    putInt(bytes.length)
    bytes.foreach { b => putByte(b) }
  }

  override def putByte(b: Byte): Unit = {
    data.setInt8(index, b)
    index += 1
  }

  override def putInt(i: Int): Unit = {
    data.setInt32(index, i, littleEndian = false)
    index += 4
  }

  override def putFloat(f: Float): Unit = {
    data.setFloat32(index, f, littleEndian = false)
    index += 4
  }

  override def putMiddleData(d: MiddleData): Unit = {
    throw new NotImplementedError()
  }

  override def getString(): String = {
    val len = getInt()
    val bytes = new Array[Byte](len)
    for (i <- 0 until len) {
      bytes(i) = getByte()
    }
    new String(bytes, "utf-8")
  }

  override def getByte(): Byte = {
    val b = data.getInt8(index)
    index += 1
    b
  }

  override def getInt(): Int = {
    val i = data.getInt32(index, littleEndian = false)
    index += 4
    i
  }

  override def getFloat(): Float = {
    val f = data.getFloat32(index, littleEndian = false)
    index += 4
    f
  }

  override def getMiddleData(): MiddleData = {
    throw new NotImplementedError()
  }

}
