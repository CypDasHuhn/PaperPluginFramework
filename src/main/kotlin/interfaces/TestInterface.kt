package interfaces

import interfaces.TestInterface.interfaceName
import interfaces.general.ClickableItem
import interfaces.general.ContextDTO
import interfaces.general.Interface
import interfaces.general.openTargetInterface
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object TestInterface : Interface("test", listOf(
    ClickableItem(
        condition = { slot, _ -> slot == 9 },
        itemStackCreator = { _, _ -> ItemStack(Material.GRASS_BLOCK) },
        action = { _, context, event -> event.whoClicked.sendMessage((context as TestInterfaceContext).itemName) }
    ),
    ClickableItem(
        condition = { slot, context -> slot < (context as TestInterfaceContext).amount },
        itemStackCreator = { slot, context -> ItemStack(Material.RED_STAINED_GLASS_PANE) },
        action = { slot, context, event ->
        }
    ),
    ClickableItem(
        condition = { slot, context -> slot == 18 },
        itemStackCreator = { slot, context -> ItemStack(Material.OAK_BUTTON) },
        action = { slot, context, event ->
            val testContext = (context as TestInterfaceContext)
            if (event.isLeftClick && context.amount < 9) {
                testContext.amount++
            } else if (context.amount > 0) {
                testContext.amount--
            }
            openTargetInterface(event.whoClicked as Player, interfaceName, testContext)
        }
    )
)) {
    override fun getInventory(player: Player?, context: ContextDTO?): Inventory {
        return Bukkit.createInventory(null, 9*6, interfaceName)
    }
}

class TestInterfaceContext(
    var amount: Int,
    val itemName: String
) : ContextDTO()