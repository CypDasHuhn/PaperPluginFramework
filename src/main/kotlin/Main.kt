import commands.general.Command
import commands.general.Completer
import commands.general.getLabels
import database.initDatabase
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ConfigurationBuilder

class Main : JavaPlugin() {

    override fun onEnable() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        val pluginManager = Bukkit.getPluginManager()
        for (listener in getListeners(this)) {
            val s = listener
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

    private fun getListeners(plugin: Plugin): List<Listener> {
        val reflections = Reflections(
            ConfigurationBuilder()
                .forPackages("listeners")
                .addScanners(SubTypesScanner(false))
        )

        val listenerClasses = reflections.getSubTypesOf(Listener::class.java)

        val listenerInstances = mutableListOf<Listener>()
        listenerClasses.forEach { clazz ->
            try {
                val instance = clazz.getDeclaredConstructor().newInstance() as Listener
                println("Instantiated listener: ${clazz.name}")
                listenerInstances.add(instance)
            } catch (e: Exception) {
                println("Error instantiating listener: ${clazz.name}")
                e.printStackTrace()
            }
        }

        listenerInstances.forEach { listener ->
            plugin.server.pluginManager.registerEvents(listener, plugin)
        }
        return listenerInstances
    }
}