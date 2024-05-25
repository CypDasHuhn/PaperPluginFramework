import commands.interfaceCommand
import commands.languageCommand
import frame.Core
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
        Core.initialize(this)
    }
}