package interfaces

import frame.`interface`.ClickableItem
import frame.`interface`.Context
import frame.`interface`.Interface
import frame.`interface`.openTargetInterface
import interfaces.TestInterface.interfaceName
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object TestInterface : Interface("test", TestInterfaceContext::class, listOf(
    ClickableItem(
        condition = { slot, _ -> slot == 9 },
        itemStackCreator = { _, _ -> ItemStack(Material.GRASS_BLOCK) },
        action = { _, context, event ->
            event.whoClicked.sendMessage((context as TestInterfaceContext).itemName)
        }
    ),
    ClickableItem(
        condition = { slot, context -> slot < (context as TestInterfaceContext).amount },
        itemStackCreator = { _, _ -> ItemStack(Material.RED_STAINED_GLASS_PANE) },
    ),
    ClickableItem(
        condition = { slot, _ -> slot == 18 },
        itemStackCreator = { _, _ -> ItemStack(Material.OAK_BUTTON) },
        action = { _, context, event ->
            val testContext = (context as TestInterfaceContext)
            if (event.isLeftClick && context.amount < 9) {
                testContext.amount++
            } else if (event.isRightClick && context.amount > 0) {
                testContext.amount--
            }
            openTargetInterface(event.whoClicked as Player, interfaceName, testContext)
        }
    )
)) {
    override fun getInventory(player: Player?, context: Context?): Inventory {
        return Bukkit.createInventory(null, 9 * 6, interfaceName)
    }
}

class TestInterfaceContext(
    var amount: Int,
    val itemName: String
) : Context()