package frame

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import frame.database.Language
import frame.database.globalLanguage
import frame.database.language
import org.bukkit.command.CommandSender
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


object Localization {
    fun getLocalizedMessage(locale: Language?, messageKey: String, vararg replacements: Pair<String, String?>): String {
        val cacheKey = "${locale ?: globalLanguage}_$messageKey"

        var message = Cache.getOrSet(cacheKey, null, {
            val selectedLanguage = locale ?: globalLanguage
            val resourcePath = "/locales/${selectedLanguage.toString().lowercase()}.json"
            val inputStream = javaClass.getResourceAsStream(resourcePath)
                ?: throw FileNotFoundException("Resource not found: $resourcePath")

            val gson = Gson()
            val type = object : TypeToken<Map<String, String>>() {}.type
            val localization: Map<String, String> =
                gson.fromJson(InputStreamReader(inputStream, StandardCharsets.UTF_8), type)

            localization[messageKey]
        }, 60 * 1000)

        if (message != null) {
            for ((key, value) in replacements) {
                message = message!!.replace("\${$key}", value ?: "")
            }
        } else {
            message = "Message not found"
        }

        return message!!
    }
}

fun t(messageKey: String, locale: Language?, vararg replacements: Pair<String, String?>): String {
    return Localization.getLocalizedMessage(locale ?: globalLanguage, messageKey, *replacements)
}

fun tSend(sender: CommandSender, messageKey: String, language: Language?, vararg replacements: Pair<String, String?>) {
    sender.sendMessage(t(messageKey, language, *replacements))
}

fun tSend(sender: CommandSender, messageKey: String, vararg replacements: Pair<String, String?>) {
    sender.sendMessage(t(messageKey, sender.language(), *replacements))
}

class Locale(var locale: Language?) {
    private val actualLocale: Language by lazy { locale ?: globalLanguage }
    fun t(messageKey: String, vararg replacements: Pair<String, String?>): String {
        return Localization.getLocalizedMessage(actualLocale, messageKey, *replacements)
    }

    fun tSend(sender: CommandSender, messageKey: String, vararg replacements: Pair<String, String?>) {
        sender.sendMessage(t(messageKey, *replacements))
    }
}

fun CommandSender.locale(): Locale {
    return Locale(this.language())
}
