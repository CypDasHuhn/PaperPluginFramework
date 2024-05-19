import com.google.gson.Gson
import database.Language
import database.Player
import database.globalLanguage
import java.io.FileNotFoundException

fun getLocalizedMessage(locale: Language?, messageKey: String, vararg replacements: Pair<String, String?>): String {
    val cacheKey = "${locale ?: globalLanguage}_$messageKey"

    var message = Cache.getOrSet(cacheKey, null, {
        val lowerCaseLang = (locale ?: globalLanguage).toString().lowercase()
        val resourcePath = "/locales/${lowerCaseLang}.json"
        val gson = Gson()

        val inputStream = ClassLoader.getSystemResourceAsStream(resourcePath)
            ?: throw FileNotFoundException("Resource not found: $resourcePath")

        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val localization: Map<String, String> = gson.fromJson(jsonString, Map::class.java) as Map<String, String>
        return@getOrSet localization[messageKey] as String
    }, 60 * 1000)

    if (message != null) {
        for ((key, value) in replacements) {
            message = message!!.replace("\${$key}", value ?: "")
        }
    }

    return message as String
}

fun t(messageKey: String, player: Player, vararg replacements: Pair<String, String?>): String {
    return getLocalizedMessage(player.language ?: globalLanguage, messageKey, *replacements)
}

fun t(messageKey: String, locale: Language?, vararg replacements: Pair<String, String?>): String {
    return getLocalizedMessage(locale ?: globalLanguage, messageKey, *replacements)
}

class Locale(private var locale: Language?) {
    private val actualLocale: Language by lazy { locale ?: globalLanguage }
    fun t(messageKey: String, vararg replacements: Pair<String, String?>): String {
        return getLocalizedMessage(actualLocale, messageKey, *replacements)
    }
}