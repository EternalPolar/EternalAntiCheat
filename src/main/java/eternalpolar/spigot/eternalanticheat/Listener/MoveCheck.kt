package eternalpolar.spigot.eternalanticheat.Listener

import eternalpolar.spigot.eternalanticheat.Movement.Fly.AntiFly
import eternalpolar.spigot.eternalanticheat.Utils.PlayerWarning
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
/**
 *  Eternal Polar 2025/7/27
 */
class MoveCheck(
    private val plugin: JavaPlugin,
) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if (AntiFly.check(player)) {
            event.isCancelled = true
        }
    }

    fun reloadConfig() {
        plugin.reloadConfig()
    }


}