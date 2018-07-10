package com.neo.sk.hiStream.front.chat

import org.scalajs.dom

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import mhtml._

/**
  * User: Taoz
  * Date: 7/10/2018
  * Time: 4:15 PM
  */
@JSExportTopLevel("chat.Main")
object Main {

  @JSExport
  def run(): Unit = {
    MainPage.show()
    println("hello world, i am a chat room.")
  }

}

object MainPage {


  def show(): Unit = {
    val page =
      <div>
        hello, world.
      </div>
    mount(dom.document.body, page)
  }

}
