package frame

import frame.commands.*
import frame.database.initDatabase
import frame.`interface`.registeredInterfaces
import frame.listeners.getListeners
import interfaces.TestInterface
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Table

object Core {
    lateinit var plugin: JavaPlugin
    var tables: List<Table> = ArrayList()
    var databasePath = plugin.dataFolder.resolve("database.db").absolutePath
    fun initialize(plugin: JavaPlugin) {
        Core.plugin = plugin
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        initDatabase(tables, databasePath)

        // listeners
        val pluginManager = Bukkit.getPluginManager()
        val listeners = getListeners(plugin)
        for (listener in listeners) {
            pluginManager.registerEvents(listener, plugin)
        }

        // commands
        rootArguments = getCommands()
        getLabels().forEach { label ->
            plugin.getCommand(label)?.let {
                it.setExecutor(Command)
                it.tabCompleter = Completer
            }
        }

        // interface
        registeredInterfaces = listOf(TestInterface)
    }
}
