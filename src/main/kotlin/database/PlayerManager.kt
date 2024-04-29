package database

import database.Players.isAdmin
import database.Players.language
import database.Players.username
import database.Players.uuid
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPlayer
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
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

fun updatePlayerLanguage(player: database.Player) {
    transaction {
        Players.update({ uuid eq player.uuid }) {
            it[language] = player.language
        }
    }
}

fun Player.isAdmin(): Boolean {
    return getPlayerByUUID(this.uniqueId.toString())?.isAdmin ?: false
}

fun Player.insertIfNotExists() {
    val player = this
    transaction {
        if (getPlayer(player.uniqueId.toString()) == null) {
            Players.insert {
                Bukkit.broadcastMessage("TEST 2")

                it[uuid] = player.uniqueId.toString()
                it[username] = player.name
            }
        }

    }
}

fun getPlayerByUUID(targetUUID: String): database.Player? {
    return transaction {
        Players.selectAll().where { uuid eq targetUUID }.firstOrNull()?.let {
            Player(
                uuid = it[uuid],
                username = it[username],
                language = it[language],
                isAdmin = it[isAdmin]
            )
        }
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

const val PLAYER_CACHE_KEY = "player"
fun cachedPlayerData(player: Player): database.Player {
    return Cache.getOrSet(
        PLAYER_CACHE_KEY,
        player,
        { getPlayerByUUID(player.uniqueId.toString()) }
    )!!
}