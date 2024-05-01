import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player

sealed class ProgressBar(
    open var progress: Double
) {
    private val players = mutableSetOf<Player>()

    protected open fun updateBar() {
        for (player in players) {
            forPlayer(player)
        }
    }

    protected abstract fun forPlayer(player: Player)

    fun forPlayers(vararg players: Player) {
        this.players.addAll(players)
        updateBar()
    }

    fun removePlayers(vararg players: Player) {
        this.players.removeAll(players)
        updateBar()
    }
}

class BossProgressBar(
    var title: String,
    var barColor: BarColor = BarColor.PURPLE,
    var barStyle: BarStyle = BarStyle.SOLID
) : ProgressBar(0.0) {
    private var bossBar: BossBar? = null

    override fun forPlayer(player: Player) {
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(title, barColor, barStyle)
        }
        bossBar?.apply {
            setTitle(title)
            color = barColor
            style = barStyle
            addPlayer(player)
        }
    }

    override var progress: Double = 0.0
        set(value) {
            field = value
            updateBar()
        }

    fun setTitle(newTitle: String) {
        title = newTitle
        bossBar = null // Creating a new boss bar instance
        updateBar()
    }

    fun setBarColor(newColor: BarColor) {
        barColor = newColor
        bossBar?.color = newColor
    }

    fun setBarStyle(newStyle: BarStyle) {
        barStyle = newStyle
        bossBar?.style = newStyle
    }

    private fun updateBar() {
        bossBar?.progress = progress
    }
}


class ExperienceProgressBar(
    progress: Double,
    var level: Int
) : ProgressBar(progress) {
    override fun forPlayer(player: Player) {
        player.level = level
        player.exp = progress.toFloat()
    }

    override var progress: Double = 0.0
        set(value) {
            field = value
            updateBar()
        }
}