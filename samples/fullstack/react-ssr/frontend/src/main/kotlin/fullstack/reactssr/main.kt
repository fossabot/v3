package fullstack.reactssr

import kotlinx.browser.document
import react.Fragment
import react.create
import react.dom.client.createRoot
import react.dom.client.hydrateRoot
import fullstack.react.ui.SampleApp


const val ssrFlag = "data-serving-mode"

fun main() {
  val target = document.getElementById("root")
  val container = if (target == null) {
    val c = document.createElement("div")
    document.body!!.appendChild(c)
    c
  } else {
    target
  }

  val application = Fragment.create() {
    SampleApp {
      message = "Hello, React SSR!"
    }
  }
  if (container.hasAttribute(ssrFlag) && container.getAttribute(ssrFlag) == "ssr") {
    console.log("Hydrating client-side (SSR flag found)")
    hydrateRoot(
      container,
      application
    )
  } else {
    console.log("Rendering client-side (no SSR flag)")
    createRoot(
      container
    ).render(
      application
    )
  }
}
