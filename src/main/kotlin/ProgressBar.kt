import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import kotlin.properties.Delegates

sealed class ProgressBar(
    open var progress: Double
) {
    val players = mutableSetOf<Player>()

    protected open fun updateBar() {
        for (player in players) {
            forPlayer(player)
        }
    }
    protected abstract fun forPlayer(player: Player)

    fun addPlayers(vararg players: Player) {
        this.players.addAll(players)
        updateBar()
    }
    fun removePlayers(vararg players: Player) {
        this.players.removeAll(players.toSet())
        updateBar()
    }
}

class BossProgressBar(
    var title: String,
    var barColor: BarColor = BarColor.PURPLE,
    var barStyle: BarStyle = BarStyle.SOLID,
) : ProgressBar(0.0){
    private var bossBar: BossBar? = null

    var pTitle: String by Delegates.observable(title) { _, _, _ -> updateBar() }
    var pBarColor: BarColor by Delegates.observable(barColor) { _, _, _ -> updateBar() }
    var pBarStyle: BarStyle by Delegates.observable(barStyle) { _, _, _ -> updateBar() }

    override var progress: Double = 0.0
        set(value) {
            field = value
            updateBar()
        }

    override fun forPlayer(player: Player) {
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(title, barColor, barStyle)
        }

        val bar = this
        bossBar?.apply {
            if (!players.contains(player)) {
                addPlayer(player)
            }
            setTitle(bar.title)
            color = bar.barColor
            style = bar.barStyle
            progress = bar.progress
        }
    }
}

class ExperienceProgressBar() : ProgressBar(0.0) {
    override var progress: Double = 0.0
        set(value) {
            field = value
            updateBar()
        }

    var level: Int = 0
        set(value) {
            field = value
            updateBar()
        }

    override fun forPlayer(player: Player) {
        player.level = level
        player.exp = progress.toFloat()
    }
}