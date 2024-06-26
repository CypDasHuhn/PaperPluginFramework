package frame.database

import com.google.gson.Gson
import frame.`interface`.Context
import frame.`interface`.getInterfaces
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

data class DBInterfaceContext(
    val interfaceName: String,
    val content: Any,
)

object InterfaceContext : IntIdTable() {
    val player = reference("player", Players, onDelete = ReferenceOption.CASCADE)
    val interfaceName = varchar("interfaceName", 50)
    val content = text("content")
}

fun ResultRow.toContext(): DBInterfaceContext {
    return DBInterfaceContext(
        interfaceName = this[InterfaceContext.interfaceName],
        content = this[InterfaceContext.content]
    )
}

fun updateContext(playerUUID: String, interfaceName: String, context: Context) {
    val gson = Gson()
    val jsonContent = gson.toJson(context)
    transaction {
        val playerId = Players.selectAll().where { Players.uuid eq playerUUID }.singleOrNull()?.get(Players.id)
        if (playerId != null) {
            val existingContext = InterfaceContext.selectAll()
                .where { (InterfaceContext.player eq playerId) and (InterfaceContext.interfaceName eq interfaceName) }
                .singleOrNull()

            if (existingContext != null) {
                InterfaceContext.update({ InterfaceContext.id eq existingContext[InterfaceContext.id] }) {
                    it[content] = jsonContent
                }
            } else {
                InterfaceContext.insert {
                    it[player] = playerId
                    it[InterfaceContext.interfaceName] = interfaceName
                    it[content] = jsonContent
                }
            }
        }
    }
}

fun readContext(player: Player, interfaceName: String): DBInterfaceContext? {
    return transaction {
        InterfaceContext.selectAll()
            .where {
                InterfaceContext.player eq player.dBPlayer().id and
                        (InterfaceContext.interfaceName eq interfaceName)
            }
            .singleOrNull()?.toContext()
    }
}

fun getContext(player: Player, interfaceName: String): Context? {
    val data = readContext(player, interfaceName) ?: return null
    val clazz = getInterfaces().firstOrNull { it.interfaceName == data.interfaceName }?.contextClass ?: return null

    val gson = Gson()
    return gson.fromJson(data.content as String, clazz.java)
}

fun <T : Context> getOrDefaultContext(player: Player, interfaceName: String, defaultSupplier: () -> T): T {
    val data = readContext(player, interfaceName) ?: return defaultSupplier()
    val clazz =
        getInterfaces().firstOrNull { it.interfaceName == data.interfaceName }?.contextClass ?: return defaultSupplier()

    val gson = Gson()
    return gson.fromJson(data.content as String, clazz.java) as T
}
