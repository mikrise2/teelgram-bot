import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.chat.Chat
import io.ktor.util.*
import kotlinx.coroutines.flow.first
import java.util.concurrent.ConcurrentHashMap


internal val users = ConcurrentHashMap<IdChatIdentifier, User>()

suspend fun BehaviourContext.getLanguage(chat: Chat): Language {
    var language = waitDataCallbackQuery(
        SendTextMessage(
            chat.id,
            "Choose your language:",
            replyMarkup = chooseLanguageButtons()
        )
    ).first().data
    while (!checkLanguage(language)) {
        language = waitDataCallbackQuery(
            SendTextMessage(
                chat.id,
                "Incorrect language, Please choose correct one:",
                replyMarkup = chooseLanguageButtons()
            )
        ).first().data
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
        val preferredName = waitText(
            getSendText(it.chat, "chooseName")
        ).first().text
        user.preferredName = preferredName
        sendMessageBundled(it.chat, "yourNameIs", user.preferredName)
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
        sendMessageBundled(it.chat, "languageChangedSuccessful")
    }
}

suspend fun main() {
    val bot = telegramBot(System.getenv("TOKEN"))
    bot.buildBehaviourWithLongPolling {
        start()
        help()
        changeLanguage()
        setMyCommands(getCommands(null))
    }.join()
}