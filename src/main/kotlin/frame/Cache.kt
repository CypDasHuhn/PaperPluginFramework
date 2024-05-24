package frame

import org.bukkit.command.CommandSender
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object Cache {
    private val playerCache: HashMap<Pair<CommandSender, String>, Any> = HashMap()
    private val generalCache: HashMap<String, Any> = HashMap()

    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    fun get(key: String, sender: CommandSender? = null): Any? {
        return if (sender != null) {
            playerCache[Pair(sender, key)]
        } else {
            generalCache[key]
        }
    }

    fun clear(key: String, sender: CommandSender? = null) {
        if (sender != null) {
            playerCache.remove(Pair(sender, key))
        } else {
            generalCache.remove(key)
        }
    }

    fun set(
        key: String,
        sender: CommandSender? = null,
        value: Any,
        clearTime: Number? = null
    ) {
        if (sender != null) {
            playerCache[Pair(sender, key)] = value
        } else {
            generalCache[key] = value
        }

        if (clearTime != null) {
            scheduler.schedule({ clear(key, sender) }, clearTime.toLong(), TimeUnit.MILLISECONDS)
        }
    }

    fun <T : Any> getOrSet(
        key: String,
        sender: CommandSender? = null,
        provider: () -> T?,
        clearTime: Number? = null
    ): T? {
        val foundValue = get(key, sender)
        return if (foundValue != null) {
            foundValue as T?
        } else {
            provider().also {
                if (it != null) {
                    set(key, sender, it, clearTime)
                }
            }
        }
    }
}