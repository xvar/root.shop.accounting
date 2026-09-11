package root.shop.accounting.server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import root.shop.accounting.shared.Greeting

// Setup-phase stub: no domain routes yet, just wiring + a health check.
// Feature routes (products, reservations, orders, QR) — and error handling
// via StatusPages — come after the per-feature architecture discussion,
// once there's real behavior to configure. See docs/design.md.
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CallLogging)
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/health") {
            call.respondText(Greeting.message())
        }
    }
}
