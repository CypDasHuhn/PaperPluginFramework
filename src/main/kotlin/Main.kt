import commands.general.Command
import commands.general.Completer
import commands.general.getLabels
import database.initDatabase
import io.github.classgraph.ClassGraph
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    override fun onEnable() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        val pluginManager = Bukkit.getPluginManager()
        for (listener in getListeners()) {
            pluginManager.registerEvents(listener, this)
        }

        getLabels().forEach { label ->
            getCommand(label)?.let {
                it.setExecutor(Command)
                it.tabCompleter = Completer
            }
        }

        initDatabase(this)
    }

    private fun getListeners(): List<Listener> {
        val scanResult = ClassGraph().enableAllInfo().scan()
        scanResult.use {
            @Suppress("UNCHECKED_CAST")
            return it.getSubclasses(Listener::class.java).loadClasses().toList() as List<Listener>
        }
    }
}