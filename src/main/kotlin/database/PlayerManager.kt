package database

import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

val DEFAULT_LANG = Language.EN

enum class Language {
    DE,
    EN,
    PL
}

fun insertPlayer(player: Player) {
    transaction {
        Users.insert {
            it[uuid] = player.uniqueId.toString()
            it[username] = player.name
            it[language] = DEFAULT_LANG.toString()
        }
    }
}

fun updatePlayerLanguage(player: database.Player) {
    transaction {
        Users.update({ Users.uuid eq player.uuid }) {
            it[language] = player.language
        }
    }
}

data class Player(val uuid: String, val username: String, val language: String)

object Players : IntIdTable() {
    val uuid = varchar("uuid", 50)
    val username = varchar("username", 50)
    val language = varchar("language", 50)
}