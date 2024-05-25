package frame.listeners

import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ConfigurationBuilder

fun getListeners(plugin: Plugin): List<Listener> {
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