package frame.`interface`

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

typealias Slot = Int

/** A [ClickableItem] is a model of an [ItemStack] inside an [Interface] which can be interacted with.*/
data class ClickableItem(
    val condition: (Slot, Context) -> Boolean,
    val itemStackCreator: (Slot, Context) -> ItemStack,
    val action: (Click, Context, InventoryClickEvent) -> Unit = { _, _, _ -> }
)

data class Click(
    var event: InventoryClickEvent,
    var player: Player,
    var item: ItemStack?,
    var material: Material?,
    var slot: Int
) {
    val isEmpty = lazy { item != null }
}

/** A class meant to be overwritten to implement the specific needs four your interface.
 * Basically some sort of value that is being saved in between clicks, to save the current state of the interface. */
open class Context

/** An Instance of Interface is a model of a UI component.
 * It's main ingredient is [clickableItems], which get resolved dynamically.
 * The field [interfaceName] is the key connected to the particular Interface. */
open class Interface(
    val interfaceName: String,
    val contextClass: KClass<out Context>,
    val clickableItems: List<ClickableItem>,
    val cancelEvent: (Click, Context, InventoryClickEvent) -> Boolean = { _, _, _ -> true },
    val ignorePlayerInventory: Boolean = true,
    val ignoreEmptySlots: Boolean = true
) {
    /** The generator of this interface. Its intended use is to be overwritten, for actual customization
     * The fields [player] and [context] by default aren't actually used, but by that you can use them if you want too. */
    open fun getInventory(player: Player?, context: Context?): Inventory {
        return Bukkit.createInventory(null, 9, interfaceName)
    }
}
