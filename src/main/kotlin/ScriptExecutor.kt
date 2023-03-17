import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.stream.Collectors

val clientId: String = System.getenv("JDOODLE_ID")
val clientSecret: String = System.getenv("JDOODLE_SECRET")

fun startScript(script: String): String? {
    val language = "nodejs"
    val versionIndex = "0"
    try {
        val url = URL("https://api.jdoodle.com/v1/execute")
        val connection = url.openConnection() as HttpURLConnection
        connection.doOutput = true
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        val input =
            "{\"clientId\": \"$clientId\",\"clientSecret\":\"$clientSecret\",\"script\":\"" + script.replace(
                "\r",
                ""
            ).replace("\n", "\\n").replace("\"","\\\"") +
                    "\",\"language\":\"" + language + "\",\"versionIndex\":\"" + versionIndex + "\"} "
        val outputStream = connection.outputStream
        outputStream.write(input.toByteArray())
        outputStream.flush()
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw RuntimeException("Please check your inputs : HTTP error code : " + connection.responseCode)
        }
        val bufferedReader = BufferedReader(
            InputStreamReader(
                connection.inputStream
            )
        )
        val output = bufferedReader.lines().collect(Collectors.joining())
        connection.disconnect()
        val parser: Parser = Parser.default()
        val stringBuilder: StringBuilder = StringBuilder(output)
        val json: JsonObject = parser.parse(stringBuilder) as JsonObject
        return json.string("output")
    } catch (e: Exception) {
        return null
    }
}

suspend fun BehaviourContext.javascript() {
    onCommand("javascript") {
        val script = waitText(
            it.chat,
            getSendText(it.chat, "javascript_request")
        )
        val result = startScript(script)
        result?.let { output ->
            sendMessage(it.chat.id, output)
            return@onCommand
        }
        sendMessageBundled(it.chat, "javascript_fail")
    }
}
