package database

import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

lateinit var plugin: JavaPlugin

fun initDatabase(pPlugin: JavaPlugin) {
    plugin = pPlugin

    val databasePath = pPlugin.dataFolder.resolve("database.db").absolutePath

    Database.connect("jdbc:sqlite:$databasePath", "org.sqlite.JDBC")

    transaction {
        SchemaUtils.createMissingTablesAndColumns(Players)
    }
}
