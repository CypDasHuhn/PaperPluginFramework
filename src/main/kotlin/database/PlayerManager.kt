package database

import Cache
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
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

data class Player(
    val uuid: String,
    var username: String,
    var language: String,
    var isAdmin: Boolean = false
)

object Players : IntIdTable() {
    val uuid = varchar("uuid", 50)
    val username = varchar("username", 50)
    val language = varchar("language", 50).nullable().default(null)
    val isAdmin = bool("isAdmin").default(false)
}


fun ResultRow.toPlayer(): Player {
    return Player(
        uuid = this[Players.uuid],
        username = this[Players.username],
        language = this[Players.language] ?: globalLanguage.toString(),
        isAdmin = this[Players.isAdmin]
    )
}

fun List<ResultRow>.toPlayers(): List<Player> {
    return this.map { it.toPlayer() }
}


fun getPlayerByUUID(targetUUID: String): Player? {
    return transaction {
        Players.selectAll().where { Players.uuid eq targetUUID }
            .firstOrNull()?.toPlayer()
    }
}

fun getPlayerByName(targetUserName: String): Player? {
    return transaction {
        Players.selectAll().where { Players.username eq targetUserName }
            .firstOrNull()?.toPlayer()
    }
}

fun getPlayers(): List<Player> {
    return transaction {
        Players.selectAll().toList().toPlayers()
    }
}

const val PLAYER_CACHE_KEY = "player"
fun cachedPlayerData(player: org.bukkit.entity.Player): Player {
    return Cache.getOrSet(
        PLAYER_CACHE_KEY,
        player,
        { getPlayerByUUID(player.uniqueId.toString()) }
    )
}

const val GLOBAL_LANGUAGE_KEY = "global_language"
fun CommandSender.language(): Language {
    return if (this is org.bukkit.entity.Player)
        Language.valueOf(cachedPlayerData(this).language)
    else globalLanguage
}

fun CommandSender.isAdmin(): Boolean {
    return if (this is org.bukkit.entity.Player)
        cachedPlayerData(this).isAdmin
    else this is ConsoleCommandSender
}

fun playerExists(userName: String): Boolean {
    return getPlayerByName(userName) != null
}

var globalLanguage: Language
    get() {
        return plugin.config.getString(GLOBAL_LANGUAGE_KEY)?.let {
            Language.valueOf(it)
        } ?: DEFAULT_LANG.also { globalLanguage = DEFAULT_LANG }
    }
    set(value) {
        val fileConfiguration = plugin.config
        fileConfiguration.set(GLOBAL_LANGUAGE_KEY, value.toString())
        plugin.saveConfig()
    }

fun org.bukkit.entity.Player.insertToDatabase() {
    val player = this
    transaction {
        val targetPlayer = getPlayerByUUID(player.uniqueId.toString())

        if (targetPlayer == null) {
            Players.insert {
                it[uuid] = player.uniqueId.toString()
                it[username] = player.name
                it[language] = DEFAULT_LANG.toString()
            }
        }
    }
}

fun Player.updateDatabase() {
    transaction {
        Players.update({ Players.uuid eq uuid }) {
            it[username] = this@updateDatabase.username
            it[language] = this@updateDatabase.language
            it[isAdmin] = this@updateDatabase.isAdmin
        }
    }
}