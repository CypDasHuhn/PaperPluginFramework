package database

import database.Players.isAdmin
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
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
        Players.insert {
            it[uuid] = player.uniqueId.toString()
            it[username] = player.name
        }
    }
}

fun updatePlayerLanguage(player: database.Player) {
    transaction {
        Players.update({ Players.uuid eq player.uuid }) {
            it[language] = player.language
        }
    }
}

fun playerIsAdmin(uuid: String): Boolean {
    return transaction {
        Players.selectAll().where(Players.uuid eq uuid).firstOrNull()?.let {
            it[isAdmin]
        } ?: false
    }
}

data class Player(
    val uuid: String,
    val username: String,
    val language: String,
    val isAdmin: Boolean = false
)

object Players : IntIdTable() {
    val uuid = varchar("uuid", 50)
    val username = varchar("username", 50)
    val language = varchar("language", 50).default(DEFAULT_LANG.toString())
    val isAdmin = bool("isAdmin").default(false)
}