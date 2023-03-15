import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.chat.Chat
import java.util.*

private val translate: Translate = TranslateOptions.getDefaultInstance().service
private val translateLanguages: Map<String, String> =
    translate.listSupportedLanguages().associate { it.name to it.code }

suspend fun BehaviourContext.noLanguage(chat: Chat, language: String) {
    sendMessageBundled(chat, "no_language", language)
}

suspend fun BehaviourContext.typeLanguage(chat: Chat, message: String): String? {
    val language = waitText(chat, getSendText(chat, message))
    val sourceLanguageCode: String? = getLanguageCodeOrNull(language)
    if (sourceLanguageCode == null) {
        noLanguage(chat, language)
    }
    return sourceLanguageCode
}

suspend fun BehaviourContext.translate() {
    onCommand("translate") {
        val sourceLanguageCode: String =
            typeLanguage(it.chat, "source_language") ?: return@onCommand
        val targetLanguageCode: String =
            typeLanguage(it.chat, "target_language") ?: return@onCommand
        val text = waitText(it.chat, getSendText(it.chat, "to_translate"))
        sendMessageBundled(it.chat, "translate")
        val translatedText = translate.translate(
            text,
            Translate.TranslateOption.sourceLanguage(sourceLanguageCode),
            Translate.TranslateOption.targetLanguage(targetLanguageCode)
        ).translatedText
        sendTextMessage(it.chat.id, translatedText)
    }
}

fun getLanguageCodeOrNull(language: String): String? {
    val languageCapitalized =
        language.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    if (languageCapitalized !in translateLanguages) {
        val languageDetection = translate.detect(languageCapitalized).language ?: return null
        val languageEnglish = translate.translate(
            languageCapitalized,
            Translate.TranslateOption.sourceLanguage(languageDetection),
            Translate.TranslateOption.targetLanguage("en")
        ).translatedText
        if (languageEnglish in translateLanguages) {
            return translateLanguages[languageEnglish]
        }
    }
    return translateLanguages[languageCapitalized]
}