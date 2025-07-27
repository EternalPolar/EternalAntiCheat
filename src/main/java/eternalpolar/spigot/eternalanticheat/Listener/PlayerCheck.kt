package eternalpolar.spigot.eternalanticheat.Listener

import eternalpolar.spigot.eternalanticheat.Movement.Fly.AntiFly
import eternalpolar.spigot.eternalanticheat.Player.Click.AntiAutoClick
import eternalpolar.spigot.eternalanticheat.Utils.PlayerWarning
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
/**
 *  Eternal Polar 2025/7/27
 */
class PlayerCheck(
    private val plugin: JavaPlugin,
    private val playerWarning: PlayerWarning
) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerClick(event: PlayerInteractEvent) {
        val player = event.player
        val clickType = if (event.action.name.contains("LEFT")) "LEFT" else "RIGHT"
        if (AntiAutoClick.checkPlayerClick(player, clickType)) {
            event.isCancelled = true
        }
    }

    // 玩家下线时清理数据
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        AntiFly.clearPlayerData(player)
        AntiAutoClick.clearPlayerData(player)
    }

    fun reload() {
        AntiAutoClick.reload()
    }
}