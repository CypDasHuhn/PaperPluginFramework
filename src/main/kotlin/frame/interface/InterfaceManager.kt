package frame.`interface`

import frame.Cache
import frame.database.getContext
import frame.database.updateContext
import interfaces.TestInterface
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import kotlin.reflect.KClass

typealias InterfaceName = String

lateinit var registeredInterfaces: List<Interface>

/** A map which links players to an interface, and whether its state is currently protected
 * (as to not be overwritten by the InventoryOpeningListener, which would else set it as empty. This needs to be performed while changing between interfaces) */
var playerInterfaceMap = HashMap<Player, InterfaceName>()
fun setPlayerEmpty(player: Player) {
    playerInterfaceMap[player] = ""
}

/** This function returns every instance of [Interface].*/
fun getInterfaces(): List<Interface> {
    return listOf(TestInterface)
    /*
    val scanResult = ClassGraph().enableAllInfo().scan()
    scanResult.use {
        @Suppress("UNCHECKED_CAST")
        return it.getSubclasses(Interface::class.java).loadClasses().toList()
    }*/
}

const val CHANGES_INTERFACE_KEY = "changes_interface"

/** This function opens the interface it could find depending on the [interfaceName]
 * for the given [player] applied with the current state of the interface ([context]). */
fun openTargetInterface(player: Player, interfaceName: String, context: Context) {
    Cache.set(CHANGES_INTERFACE_KEY, player, true, 1000)

    val targetInterface =
        registeredInterfaces.firstOrNull { currentInterface -> currentInterface.interfaceName == interfaceName }
            ?: return

    playerInterfaceMap[player] = interfaceName

    val inventory = targetInterface.getInventory(player, context).fillInventory(targetInterface.clickableItems, context)

    updateContext(player.uniqueId.toString(), interfaceName, context)

    player.openInventory(inventory)
}

/** This function fills the [Inventory] with [ItemStack]'s using the registered [clickableItems]'s
 * and the current Interface State ([context]). */
private fun Inventory.fillInventory(clickableItems: List<ClickableItem>, context: Context): Inventory {
    for (slot in 0 until this.size) {
        clickableItems.firstOrNull { currentItem -> currentItem.condition(slot, context) }?.let {
            this@fillInventory.setItem(slot, it.itemStackCreator(slot, context))
        }
    }
    return this
}

fun handleInventoryClick(event: InventoryClickEvent) {
    val player = event.whoClicked as Player
    val interfaceName = playerInterfaceMap[player] ?: return
    if (interfaceName.isEmpty()) return



    val correspondingInterface = getInterfaces()
        .firstOrNull { currentInterface -> currentInterface.interfaceName == interfaceName }
        .run {
            this ?: return
        }

    if (event.currentItem == null && correspondingInterface.ignoreEmptySlots) return
    if (event.inventory is PlayerInventory && correspondingInterface.ignorePlayerInventory) return

    val click = Click(event, player, event.currentItem, event.currentItem?.type, event.slot)

    val context = getContext(player, interfaceName) ?: Context()

    event.isCancelled = correspondingInterface.cancelEvent(click, context, event)

    correspondingInterface.clickableItems
        .filter { currentItem -> currentItem.condition(click.slot, context) }
        .forEach { currentItem ->
            currentItem.action(click, context, event)
        }
}