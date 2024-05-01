package listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

object PlayerInteractEvent : Listener {
    @EventHandler
    fun listener(event: PlayerInteractEvent) {
        val usableItems: List<UsableItem> = listOf()

        for (usableItem in usableItems) {
            if (usableItem.condition(usableItem, event)) {
                usableItem.clickEffect(usableItem, event)
            }
        }

        
    }
}