package interfaces.general

import Cache
import database.updateContext
import interfaces.TestInterface
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
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
fun openTargetInterface(player: Player, interfaceName: String, context: ContextDTO) {
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
private fun Inventory.fillInventory(clickableItems: List<ClickableItem>, context: ContextDTO): Inventory {
    for (slot in 0 until this.size) {
        clickableItems.firstOrNull { currentItem -> currentItem.pCondition(slot, context) }?.let {
            this@fillInventory.setItem(slot, it.pItemStackCreator(slot, context))
        }
    }
    return this
}

typealias Slot = Int

/** A [ClickableItem] is a model of an [ItemStack] inside an [Interface] which can be interacted with.*/
class ClickableItem// Using Any to accept both Boolean and Unit actions
<T>(
    condition: (Slot, ContextDTO) -> Boolean,
    itemStackCreator: (Slot, ContextDTO) -> ItemStack,
    action: (ClickDTO, ContextDTO, InventoryClickEvent) -> T
) {
    var pCondition: (Slot, ContextDTO) -> Boolean = condition
    var pItemStackCreator: (Slot, ContextDTO) -> ItemStack = itemStackCreator
    var pAction: (ClickDTO, ContextDTO, InventoryClickEvent) -> Any = when (action) {
        is (ClickDTO, ContextDTO, InventoryClickEvent) -> Boolean -> action
        is (ClickDTO, ContextDTO, InventoryClickEvent) -> Unit -> { click, context, event ->
            action(click, context, event)
            true
        }

        else -> throw IllegalArgumentException("Invalid action type")
    }
}

data class ClickDTO(
    var event: InventoryClickEvent,
    var player: Player,
    var item: ItemStack,
    var material: Material,
    var slot: Int
)

/** A class meant to be overwritten to implement the specific needs four your interface.
 * Basically some sort of value that is being saved in between clicks, to save the current state of the interface. */
open class ContextDTO

/** An Instance of Interface is a model of a UI component.
 * It's main ingredient is [clickableItems], which get resolved dynamically.
 * The field [interfaceName] is the key connected to the particular Interface. */
open class Interface(
    val interfaceName: String,
    val contextClass: KClass<out ContextDTO>,
    val clickableItems: List<ClickableItem>,
    val cancelEvent: (ClickDTO, ContextDTO, InventoryClickEvent) -> Boolean = { _, _, _ -> true }
) {
    /** The generator of this interface. Its intended use is to be overwritten, for actual customization
     * The fields [player] and [context] by default aren't actually used, but by that you can use them if you want too. */
    open fun getInventory(player: Player?, context: ContextDTO?): Inventory {
        return Bukkit.createInventory(null, 9, interfaceName)
    }
}


