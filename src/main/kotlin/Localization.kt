import com.google.gson.Gson
import database.Player
import java.io.File

fun getLocalizedMessage(locale: String, messageKey: String): String? {
    val jsonFile = File("resources/locales/$locale.json")
    val jsonString = jsonFile.readText()
    val gson = Gson()

    val localization: Map<String, String> = gson.fromJson(jsonString, Map::class.java) as Map<String, String>

    return localization[messageKey]
}

fun t(messageKey: String, player: Player): String? {
    return getLocalizedMessage(messageKey, player.language)
}

class Locale(private val locale: String) {
    fun t(messageKey: String) {
        getLocalizedMessage(messageKey, locale)
    }
}