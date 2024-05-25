package frame.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

fun initDatabase(tables: List<Table>, databasePath: String) {
    Database.connect("jdbc:sqlite:$databasePath", "org.sqlite.JDBC")

    transaction {
        SchemaUtils.createMissingTablesAndColumns(Players, InterfaceContext, *tables.toTypedArray())
    }
}
