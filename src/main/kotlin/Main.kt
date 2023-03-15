import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.*
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.data
import dev.inmo.tgbotapi.requests.abstracts.Request
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.chat.Chat
import io.ktor.util.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.util.concurrent.ConcurrentHashMap


internal val users = ConcurrentHashMap<IdChatIdentifier, User>()

suspend fun BehaviourContext.waitText(chat: Chat, request: Request<*>) =
    waitTextMessage(request).filter { message -> message.chat == chat }.first().content.text


suspend fun BehaviourContext.getLanguage(chat: Chat): Language {
    var language = waitMessageCallbackQuery(
        SendTextMessage(
            chat.id,
            "Choose your language:",
            replyMarkup = chooseLanguageButtons()
        )
    ).filter { it.message.chat == chat }.first().data ?: ""
    while (!checkLanguage(language)) {
        language = waitMessageCallbackQuery(
            SendTextMessage(
                chat.id,
                "Incorrect language, Please choose correct one:",
                replyMarkup = chooseLanguageButtons()
            )
        ).filter { it.message.chat == chat }.first().data ?: ""
    }
    return Language.valueOf(language.toUpperCasePreservingASCIIRules())
}

suspend fun BehaviourContext.start() {
    onCommand("start") {
        val language = getLanguage(it.chat)
        createIfNotExist(it.chat)
        val user = users[it.chat.id]!!
        user.language = language
        setMyCommands(getCommands(it.chat))
        val preferredName = waitText(it.chat, getSendText(it.chat, "choose_name"))
        user.preferredName = preferredName
        sendMessageBundled(it.chat, "your_name_is", user.preferredName)
    }
}

suspend fun BehaviourContext.help() {
    onCommand("help") {
        sendMessageBundled(it.chat, "help")
    }
}

suspend fun BehaviourContext.changeLanguage() {
    onCommand("change_language") {
        val user = createIfNotExist(it.chat)
        val language = getLanguage(it.chat)
        user.language = language
        setMyCommands(getCommands(it.chat))
        sendMessageBundled(it.chat, "language_changed_successful")
    }
}

suspend fun BehaviourContext.changeName() {
    onCommand("change_name") {
        val user = createIfNotExist(it.chat)
        val preferredName = waitText(it.chat, getSendText(it.chat, "choose_name"))
        user.preferredName = preferredName
        sendMessageBundled(it.chat, "your_name_is", user.preferredName)
    }
}

suspend fun main() {
    val bot = telegramBot(System.getenv("TOKEN"))
    bot.buildBehaviourWithLongPolling {
        start()
        help()
        changeLanguage()
        changeName()
        translate()
        setMyCommands(getCommands(null))
    }.join()
}

