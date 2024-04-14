package listeners

import interfaces.general.setPlayerEmpty
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

object InventoryCloseListener : Listener {
    @EventHandler
    fun listener(event: InventoryCloseEvent) {
        setPlayerEmpty(event.player as Player)
    }
}