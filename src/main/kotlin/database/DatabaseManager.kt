package database

import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun initDatabase(plugin: JavaPlugin) {
    val databasePath = plugin.dataFolder.resolve("database.db").absolutePath

    Database.connect("jdbc:sqlite:$databasePath", "org.sqlite.JDBC")

    transaction {
        SchemaUtils.createMissingTablesAndColumns(Users)
    }
}

fun insertTestData() {
    val testData = listOf(
        TestUser("JohnDoe", "55AS-ASDA", Languages.DE.toString()),
        TestUser("JaneDoa", "ASD3-29eF", Languages.EN.toString())
    )

    transaction {
        for (user in testData) {
            Users.insert {
                it[username] = user.username
                it[uuid] = user.uuid
                it[language] = user.language
            }
        }
    }
}

fun playerByUUID(uuid: String): TestUser? {
    return transaction {
        Users.selectAll().where { Users.uuid eq uuid }.singleOrNull()?.let {
            TestUser(
                username = it[Users.username],
                uuid = it[Users.uuid],
                language = it[Users.language]
            )
        }
    }
}

data class TestUser(val username: String, val uuid: String, val language: String)

object Users : IntIdTable() {
    val uuid = varchar("uuid", 50)
    val username = varchar("username", 50)
    val language = varchar("language", 50)
}

