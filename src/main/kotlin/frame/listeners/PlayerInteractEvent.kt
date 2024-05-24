package frame.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

object PlayerInteractEvent : Listener {
    @EventHandler
    fun listener(event: PlayerInteractEvent) {
        val usableItems: List<UsableItem> = listOf() // Assuming this will be populated with actual items

        for (usableItem in usableItems) {
            if (usableItem.condition(event)) {
                var currentEffects: List<ItemEffect> = usableItem.subEffects

                while (true) {
                    var foundEffect = false

                    for (effect in currentEffects) {
                        if (effect.condition(event)) {
                            foundEffect = true
                            if (effect.subEffects.isNotEmpty()) {
                                currentEffects = effect.subEffects
                            } else {
                                effect.clickEffect(event)
                                break
                            }
                            break
                        }
                    }

                    if (!foundEffect) {
                        usableItem.clickEffect(event)
                        break
                    }
                }
                break
            }
        }
    }
}