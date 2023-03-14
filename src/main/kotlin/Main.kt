import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.utils.matrix
import dev.inmo.tgbotapi.utils.row
import io.ktor.util.*
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.ConcurrentHashMap


private val users = ConcurrentHashMap<IdChatIdentifier, User>()

enum class Language(val languageName: String, val locale: Locale) {
    RUSSIAN("Russian", Locale("ru")), ENGLISH("English", Locale("en")), GERMAN(
        "German",
        Locale("de")
    )
}

private val languages = Language.values().map { it.languageName }

data class User(var preferredName: String = "user", var language: Language = Language.ENGLISH)

fun checkLanguage(language: String) = language in languages

fun chooseLanguageButtons(): InlineKeyboardMarkup {
    return InlineKeyboardMarkup(matrix {
        for (language in languages) {
            row {
                dataButton(language, language)
            }
        }
    })
}

suspend fun BehaviourContext.start() {
    onCommand("start") {
        var language = waitDataCallbackQuery(
            SendTextMessage(
                it.chat.id,
                "Choose your language:",
                replyMarkup = chooseLanguageButtons()
            )
        ).first().data
        while (!checkLanguage(language)) {
            language = waitDataCallbackQuery(
                SendTextMessage(
                    it.chat.id,
                    "Incorrect language, Please choose correct one:",
                    replyMarkup = chooseLanguageButtons()
                )
            ).first().data
        }
        users[it.chat.id] =
            User(language = Language.valueOf(language.toUpperCasePreservingASCIIRules()))
        val user = users[it.chat.id]!!
        val bundle = ResourceBundle.getBundle("messages", user.language.locale)
        val preferredName = waitText(
            SendTextMessage(it.chat.id, bundle.getString("chooseName"))
        ).first().text
        user.preferredName = preferredName
        sendTextMessage(it.chat.id, bundle.getString("yourNameIs").format(user.preferredName))
    }
}

suspend fun main() {
    val bot = telegramBot(System.getenv("TOKEN"))
    bot.buildBehaviourWithLongPolling {
        start()
    }.join()
}