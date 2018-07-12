package com.neo.sk.hiStream.front.chat

import com.neo.sk.hiStream.chat.MiddleBuffer

import scala.scalajs.js

/**
  * User: Taoz
  * Date: 7/12/2018
  * Time: 11:02 AM
  */
class MiddleBufferInJs private () extends MiddleBuffer {


  private[this] var data: js.typedarray.DataView = _
  private[this] var index: Int = -1


  override def clear(): Unit = {
    index = 0
  }

  def this(array: js.typedarray.ArrayBuffer) {
    this()
    data = new js.typedarray.DataView(array)
    index = 0
  }

  def this(size: Int) {
    this()
    val in = new js.typedarray.ArrayBuffer(size)
    data = new js.typedarray.DataView(in)
    index = 0
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

  override def result(): js.typedarray.ArrayBuffer = {
    data.buffer.slice(0, index)
  }
}
