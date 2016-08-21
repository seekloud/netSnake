package tutorial.webapp

import org.scalajs.dom._
import org.scalajs.jquery.jQuery

import scala.scalajs.js.JSApp

/**
  * User: Taoz
  * Date: 8/20/2016
  * Time: 10:12 PM
  */
object TutorialApp2 {
  @scala.scalajs.js.annotation.JSExport
  def main(): Unit = {
    jQuery(setupUI _ )
  }


  def setupUI(): Unit = {
    jQuery("""<button type="button">clickMe.</button>""")
      .click(addClickMessage _)
      .appendTo(jQuery("body"))
    jQuery("body").append("<p>hello, world.</p>")
  }



  def addClickMessage(): Unit = {
    jQuery("body").append("<p>you click me...</p>")
  }



}
