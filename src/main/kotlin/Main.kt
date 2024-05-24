import commands.interfaceCommand
import commands.languageCommand
import frame.commands.Command
import frame.commands.Completer
import frame.commands.getLabels
import frame.commands.rootArguments
import frame.database.initDatabase
import frame.`interface`.registeredInterfaces
import frame.listeners.InventoryClickListener
import frame.listeners.InventoryCloseListener
import frame.listeners.PlayerInteractEvent
import frame.listeners.PlayerJoinEvent
import interfaces.TestInterface
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ConfigurationBuilder

class Main : JavaPlugin() {


    override fun onEnable() {
        plugin = this
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        rootArguments = mutableListOf(interfaceCommand, languageCommand)
        val pluginManager = Bukkit.getPluginManager()

        val listeners = listOf(InventoryClickListener, InventoryCloseListener, PlayerInteractEvent, PlayerJoinEvent)
        for (listener in listeners) {
            pluginManager.registerEvents(listener, this)
        }

        getLabels().forEach { label ->
            getCommand(label)?.let {
                it.setExecutor(Command)
                it.tabCompleter = Completer
            }
        }

        registeredInterfaces = listOf(TestInterface)

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

    companion object {
        lateinit var plugin: JavaPlugin
    }
}