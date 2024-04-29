import org.bukkit.entity.Player
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object  Cache {
    private val playerCache: HashMap<Pair<Player, String>, Any> = HashMap()
    private val generalCache: HashMap<String, Any> = HashMap()

    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    fun get(key: String, player: Player? = null): Any? {
        return if (player!= null) {
            playerCache[Pair(player, key)]
        } else {
            generalCache[key]
        }
    }

    fun clear(key: String, player: Player? = null) {
        if (player!= null) {
            playerCache.remove(Pair(player, key))
        } else {
            generalCache.remove(key)
        }
    }

    fun set(key: String, player: Player? = null, value: Any, clearTime: Number? = null) {
        if (player!= null) {
            playerCache[Pair(player, key)] = value
        } else {
            generalCache[key] = value
        }

        if (clearTime != null) {
            scheduler.schedule({ clear(key, player) }, clearTime.toLong(), TimeUnit.MILLISECONDS)
        }
    }

    fun <T : Any> getOrSet(key: String, player: Player? = null, provider: () -> T?, clearTime: Number? = null): T? {
        return  get(key, player) as T? ?:
                provider().also {
                    set(key, player, this, clearTime)
                }
    }
}