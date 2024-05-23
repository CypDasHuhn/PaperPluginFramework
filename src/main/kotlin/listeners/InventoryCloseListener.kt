package listeners

import Cache
import interfaces.general.CHANGES_INTERFACE_KEY
import interfaces.general.setPlayerEmpty
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

object InventoryCloseListener : Listener {
    @EventHandler
    fun listener(event: InventoryCloseEvent) {
        if (Cache.get(CHANGES_INTERFACE_KEY, event.player) as Boolean? == true) {
            Cache.clear(CHANGES_INTERFACE_KEY, event.player)
            return
        }
        setPlayerEmpty(event.player as Player)
    }
}