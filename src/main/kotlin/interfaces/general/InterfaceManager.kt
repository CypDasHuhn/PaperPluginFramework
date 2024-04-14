package interfaces.general

import io.github.classgraph.ClassGraph
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

typealias InterfaceName = String

/** A map which links players to an interface, and whether its state is currently protected
 * (as to not be overwritten by the InventoryOpeningListener, which would else set it as empty. This needs to be performed while changing between interfaces) */
var playerInterfaceMap = HashMap<Player, InterfaceName>()
fun setPlayerEmpty(player: Player) {
    playerInterfaceMap[player] = ""
}

/** This function returns every instance of [Interface].*/
fun getInterfaces(): List<Interface> {
    val scanResult = ClassGraph().enableAllInfo().scan()
    scanResult.use {
        @Suppress("UNCHECKED_CAST")
        return it.getSubclasses(Interface::class.java).loadClasses().toList() as List<Interface>
    }
}

/** This function opens the interface it could find depending on the [interfaceName]
 * for the given [player] applied with the current state of the interface ([context]). */
fun openTargetInterface(player: Player, interfaceName: String, context: ContextDTO) {
    val targetInterface =
        getInterfaces().stream().filter { currentInterface -> currentInterface.interfaceName == interfaceName }
            .findFirst().run {
                if (isPresent) {
                    get()
                } else return
            }

    playerInterfaceMap[player] = interfaceName

    val inventory =
        targetInterface.getInventory(player, context).fillInventory(targetInterface.clickableItems, context)
}

/** This function fills the [Inventory] with [ItemStack]'s using the registered [clickableItems]'s
 * and the current Interface State ([context]). */
private fun Inventory.fillInventory(clickableItems: List<ClickableItem>, context: ContextDTO) {
    for (slot in 0..this.size) {
        clickableItems.stream().filter { currentItem -> currentItem.condition(slot, context) }.findFirst().run {
            if (isPresent) {
                this@fillInventory.setItem(slot, get().itemStackCreator(slot, context))
            }
        }
    }
}

typealias Slot = Int

/** A [ClickableItem] is a model of an [ItemStack] inside an [Interface] which can be interacted with.*/
data class ClickableItem(
    /** controls when the program recognizes that it should be placed or got clicked. */
    var condition: (Slot, ContextDTO) -> Boolean,
    /** A function that returns an [ItemStack] when the condition was found. */
    var itemStackCreator: (Slot, ContextDTO) -> ItemStack,
    /** A function that is called when the condition was found. */
    var action: (Slot, ContextDTO) -> Unit
)

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
open class Interface(val interfaceName: String, val clickableItems: List<ClickableItem>) {
    /** The generator of this interface. Its intended use is to be overwritten, for actual customization
     * The fields [player] and [context] by default aren't actually used, but by that you can use them if you want too. */
    fun getInventory(player: Player?, context: ContextDTO?): Inventory {
        return Bukkit.createInventory(null, 9, interfaceName)
    }
}


