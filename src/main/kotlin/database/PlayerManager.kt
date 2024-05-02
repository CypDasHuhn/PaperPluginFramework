package database

import Cache
import Main
import database.Players.isAdmin
import database.Players.language
import database.Players.username
import database.Players.uuid
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPlayer
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.IntIdTable
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

fun updatePlayerAdminState(player: database.Player) {
    transaction {
        Players.update({ uuid eq player.uuid }) {
            it[language] = player.language
        }
    }
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

fun getPlayerFromDatabase(searchKey: String, searchByUUID: Boolean): database.Player? {
    return transaction {
        val query = if (searchByUUID) {
            Players.selectAll().where { uuid eq searchKey }
        } else {
            Players.selectAll().where { username eq searchKey }
        }

        query.firstOrNull()?.let {
            Player(
                uuid = it[uuid],
                username = it[username],
                language = it[language],
                isAdmin = it[isAdmin]
            )
        }
    }
}

fun getPlayerByUUID(targetUUID: String): database.Player? {
    return getPlayerFromDatabase(targetUUID, searchByUUID = true)
}

fun getPlayerByName(name: String): database.Player? {
    return getPlayerFromDatabase(name, searchByUUID = false)
}

data class Player(
    val uuid: String,
    val username: String,
    val language: String,
    var isAdmin: Boolean = false
)

object Players : IntIdTable() {
    val uuid = varchar("uuid", 50)
    val username = varchar("username", 50)
    val language = varchar("language", 50).default(DEFAULT_LANG.toString())
    val isAdmin = bool("isAdmin").default(false)
}

const val PLAYER_CACHE_KEY = "player"
fun cachedPlayerData(player: Player): database.Player {
    val s = Cache.getOrSet(
        PLAYER_CACHE_KEY,
        player,
        { getPlayerByUUID(player.uniqueId.toString()) }
    )

    return Cache.getOrSet(
        PLAYER_CACHE_KEY,
        player,
        { getPlayerByUUID(player.uniqueId.toString()) }
    )!!
}

const val GLOBAL_LANGUAGE_KEY = "global_language"
fun CommandSender.language(): Language {
    return if (this is Player)
        Language.valueOf(cachedPlayerData(this).language)
    else globalLanguage
}

fun CommandSender.isAdmin(): Boolean {
    return if (this is Player)
        cachedPlayerData(this).isAdmin
    else this is ConsoleCommandSender
}

fun playerExists(userName: String): Boolean {
    return getPlayerByName(userName) != null
}

fun getAllPlayers(): List<database.Player> {
    return transaction {
        Players.selectAll().map {
            Player(
                uuid = it[uuid],
                username = it[username],
                language = it[language],
                isAdmin = it[isAdmin]
            )
        }
    }
}

var globalLanguage: Language
    get() {
        var fileConfiguration = Main.plugin.config
        return Language.valueOf(fileConfiguration.get(GLOBAL_LANGUAGE_KEY).toString())
    }
    set(value) {
        var fileConfiguration = Main.plugin.config
        fileConfiguration.set(GLOBAL_LANGUAGE_KEY, value.toString())
    }
