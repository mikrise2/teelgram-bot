import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.usernameChatOrThrow
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.chat.Chat
import dev.inmo.tgbotapi.utils.matrix
import dev.inmo.tgbotapi.utils.row
import java.util.*

fun createIfNotExist(chat: Chat): User {
    if (!users.containsKey(chat.id)) {
        users[chat.id] = User(chat.usernameChatOrThrow().username?.username ?: "")
    }
    return users[chat.id]!!
}

enum class Language(val languageName: String, val locale: Locale) {
    RUSSIAN("Russian", Locale("ru")), ENGLISH("English", Locale("en")), GERMAN(
        "German",
        Locale("de")
    )
}

private val languages = Language.values().map { it.languageName }

data class User(
    val username: String,
    var preferredName: String = "user",
    var language: Language = Language.ENGLISH,
    val friends: List<String> = mutableListOf()
)

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

fun getCommands(chat: Chat?): List<BotCommand> {
    var bundle = ResourceBundle.getBundle("commands", Language.ENGLISH.locale)
    chat?.let {
        val user = createIfNotExist(it)
        bundle = ResourceBundle.getBundle("commands", user.language.locale)
    }
    return listOf(
        "help",
        "javascript",
        "translate",
        "add_friend",
        "share_location",
        "show_friends",
        "change_name",
        "change_language"
    ).map { BotCommand(it, bundle.getString(it)) }
}

fun getSendText(
    chat: Chat,
    bundleKey: String,
    vararg params: String
): SendTextMessage {
    val user = createIfNotExist(chat)
    val bundle = ResourceBundle.getBundle("messages", user.language.locale)
    return SendTextMessage(chat.id, bundle.getString(bundleKey).format(*params))
}

suspend fun BehaviourContext.sendMessageBundled(
    chat: Chat,
    bundleKey: String,
    vararg params: String
) {
    val user = createIfNotExist(chat)
    val bundle = ResourceBundle.getBundle("messages", user.language.locale)
    sendTextMessage(chat.id, bundle.getString(bundleKey).format(*params))
}