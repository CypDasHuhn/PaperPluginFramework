package listeners

import database.getContext
import interfaces.general.ClickDTO
import interfaces.general.ContextDTO
import interfaces.general.getInterfaces
import interfaces.general.playerInterfaceMap
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.PlayerInventory

object InventoryClickListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory is PlayerInventory) return

        val player = event.whoClicked as Player
        val interfaceName = playerInterfaceMap[player] ?: return

        if (interfaceName.isEmpty()) return

        event.isCancelled = true

        if (event.currentItem == null) return

        val correspondingInterface = getInterfaces()
            .firstOrNull { currentInterface -> currentInterface.interfaceName == interfaceName }
            .run {
                this ?: return
            }

        val clickDTO = ClickDTO(event, player, event.currentItem!!, event.currentItem!!.type, event.slot)

        val context = getContext(player, interfaceName) ?: ContextDTO()

        var cancelEvent =

            correspondingInterface.clickableItems
                .firstOrNull { currentItem -> currentItem.pCondition(clickDTO.slot, context) }
                .run {
                    this?.pAction?.let {
                        it(clickDTO, context, event)

                    } ?: return
                }
    }
}