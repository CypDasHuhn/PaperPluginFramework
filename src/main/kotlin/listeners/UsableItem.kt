package listeners

import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack


typealias IsShift = Boolean
typealias IsLeft = Boolean

enum class ClickState(
    val isShift: IsShift?,
    val isLeft: IsLeft?
) {
    CLICK(null, null),
    SHIFT_CLICK(true, null),
    NORMAL_CLICK(false, null),
    LEFT_CLICK(null, true),
    RIGHT_CLICK(null, false),
    LEFT_SHIFT_CLICK(true, true),
    RIGHT_SHIFT_CLICK(true, false),
    LEFT_NORMAL_CLICK(false, true),
    RIGHT_NORMAL_CLICK(false, false)
}

infix fun PlayerInteractEvent.matches(events: List<ClickState>) {
    
}

fun ItemStack.create(
    material: Material,
    name: String? = null,
    description: List<String>? = null,
    amount: Int = 0,
    nbt: Any? = null,
) {
}

class UsableItem(
    val bindedItem: ItemStack,
    val condition: ((UsableItem, PlayerInteractEvent) -> Boolean),
    val clickEffect: ((UsableItem, PlayerInteractEvent) -> Unit),
    vararg val subEffects: SubEffect
)

class SubEffect(
    val condition: ((PlayerInteractEvent) -> Boolean),
    val clickEffect: ((PlayerInteractEvent) -> Unit),
    vararg val subEffects: SubEffect
)
