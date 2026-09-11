package root.shop.accounting.app

import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}
