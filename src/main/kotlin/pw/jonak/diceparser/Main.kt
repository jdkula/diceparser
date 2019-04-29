package pw.jonak.diceparser

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.default
import io.ktor.content.files
import io.ktor.content.static
import io.ktor.content.staticRootFolder
import io.ktor.features.CORS
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respondText
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import kotlinx.serialization.json.JSON
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import sun.awt.SunToolkit
import java.io.File
import java.security.InvalidParameterException

const val MAX_ITERATION = 999
const val ROLL_MAX_N = 99999

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, 8080) {
        install(CORS) {
            anyHost()
        }
        routing {
            post("/dice") {
                val post = call.receiveParameters()
                val roll = post["roll"]?.replace(" ", "")
                if(roll == null) {
                    context.respondText("Bad Params", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                } else {
                    println(roll)
                    try {
                        val rolls = Roll.parse(roll).map { it.toMessage() }
                        context.respondText(JSON.stringify(RollMessage::class.serializer().list, rolls), ContentType.Application.Json)
                    } catch (e: NumberFormatException) {
                        context.respondText("Bad Dice Format", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                    } catch (e: SunToolkit.InfiniteLoop) {
                        context.respondText("Roll Caused Infinite Loop", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                    } catch (e: InvalidParameterException) {
                        context.respondText("Number of rolls too big! (max: $ROLL_MAX_N)", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                    }
                }
            }
            get("/shutdown") {
                System.exit(0)
            }

            static {
                staticRootFolder = File("./src/main/resources")
                files(".")
                static("css") {
                    staticRootFolder = File("./src/main/resources")
                    files("css")
                }
                static("js") {
                    staticRootFolder = File("./src/main/resources")
                    files("js")
                }
                static("fonts") {
                    staticRootFolder = File("./src/main/resources")
                    files("fonts")
                }
                default("index.html")
            }
        }
    }
    server.start(true)
}
