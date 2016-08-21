package tutorial.webapp

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom



/**
  * User: Taoz
  * Date: 8/20/2016
  * Time: 9:59 AM
  */
object TutorialApp{

  import org.scalajs.dom.document


  def main(): Unit = {
    appendPar(document.body, "Hello world.")
  }


  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    val textNode = document.createTextNode(text)
    parNode.appendChild(textNode)
    targetNode.appendChild(parNode)
  }

  @JSExport
  def addClickMessage(): Unit = {
    appendPar(document.body, "you click the button.")
  }

}
