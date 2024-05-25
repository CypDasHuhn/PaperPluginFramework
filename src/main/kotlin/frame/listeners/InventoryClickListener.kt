package frame.listeners

import frame.database.getContext
import frame.`interface`.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.PlayerInventory

object InventoryClickListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        handleInventoryClick(event)
    }
}