import database.initDatabase
import database.insertTestData
import io.github.classgraph.ClassGraph
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    override fun onEnable() {
        val pluginManager = Bukkit.getPluginManager()
        for (listener in getListeners()) {
            pluginManager.registerEvents(listener, this)
        }

        initDatabase(this)
        insertTestData()
    }

    private fun getListeners(): List<Listener> {
        val scanResult = ClassGraph().enableAllInfo().scan()
        scanResult.use {
            @Suppress("UNCHECKED_CAST")
            return it.getSubclasses(Listener::class.java).loadClasses().toList() as List<Listener>
        }
    }


}