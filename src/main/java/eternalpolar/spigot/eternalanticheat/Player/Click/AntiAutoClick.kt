package eternalpolar.spigot.eternalanticheat.Player.Click

import eternalpolar.spigot.eternalanticheat.Utils.PlayerWarning
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.BlockIterator
import org.bukkit.util.RayTraceResult
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.io.File
import java.util.*
import java.util.ArrayDeque
/**
 *  Eternal Polar 2025/7/27
 */
object AntiAutoClick {
    private val clickLogs = mutableMapOf<UUID, MutableMap<String, ArrayDeque<Long>>>()
    private val warningCounts = mutableMapOf<UUID, Int>()

    @Nullable
    private var plugin: JavaPlugin? = null
    @Nullable
    private var warningSystem: PlayerWarning? = null
    private lateinit var config: Config
    private var isVersion1_13OrNewer = false

    @JvmStatic
    fun init(@NotNull plugin: JavaPlugin, @NotNull warning: PlayerWarning) {
        this.plugin = plugin
        this.warningSystem = warning
        checkServerVersion()
        loadConfig()
    }

    private fun checkServerVersion() {
        isVersion1_13OrNewer = try {
            val version = Bukkit.getBukkitVersion().split('-')[0].split('.')
            val major = version[0].toInt()
            val minor = if (version.size > 1) version[1].toInt() else 0
            major > 1 || (major == 1 && minor >= 13)
        } catch (e: Exception) {
            plugin?.logger?.warning("Version detection failed, defaulting to 1.8+ compatibility mode: ${e.message}")
            false
        }
        plugin?.logger?.info("AntiAutoClick version compatibility: ${if (isVersion1_13OrNewer) "1.13+" else "1.8-1.12"}")
    }

    private fun loadConfig() {
        val plugin = this.plugin ?: return

        val configFile = File(plugin.dataFolder, "Player/AntiAutoClick.yml").apply {
            if (!exists()) {
                parentFile.mkdirs()
                plugin.saveResource("Player/AntiAutoClick.yml", false)
            }
        }

        val yaml = YamlConfiguration.loadConfiguration(configFile)

        config = Config(
            enabled = yaml.getBoolean("enabled", true),
            maxLeftCps = yaml.getInt("max-left-cps", 15),
            maxRightCps = yaml.getInt("max-right-cps", 12),
            checkWindow = yaml.getInt("check-window", 1000),
            warnThreshold = yaml.getInt("warning-threshold", 3),
            blockExemptionDistance = yaml.getDouble("block-exemption-distance", 5.0),
            flag = yaml.getString("flag", "AutoClick") ?: "AutoClick"
        )
    }

    fun checkPlayerClick(@NotNull player: Player, clickType: String): Boolean {
        // 创造模式、旁观模式、OP不检测
        if (!config.enabled || isExempt(player)) {
            return false
        }

        if (clickType.equals("LEFT", ignoreCase = true) && isLookingAtBlock(player)) {
            return false
        }

        val uuid = player.uniqueId
        val now = System.currentTimeMillis()
        val type = if (clickType.equals("LEFT", ignoreCase = true)) "LEFT" else "RIGHT"

        val playerLogs = clickLogs.getOrPut(uuid) { mutableMapOf() }
        val clickQueue = playerLogs.getOrPut(type) { ArrayDeque() }

        clickQueue.add(now)
        while (clickQueue.isNotEmpty() && now - clickQueue.first > config.checkWindow) {
            clickQueue.poll()
        }

        return handleClickCheck(player, type, clickQueue.size)
    }

    // 判断是否为豁免玩家（创造、旁观、OP）
    private fun isExempt(player: Player): Boolean {
        return player.gameMode == GameMode.CREATIVE ||
                player.gameMode == GameMode.SPECTATOR ||
                player.isOp
    }

    private fun isLookingAtBlock(player: Player): Boolean {
        return try {
            val maxDistance = config.blockExemptionDistance.coerceAtMost(10.0)

            if (isVersion1_13OrNewer) {
                val eyeLocation: Location = player.eyeLocation
                val result: RayTraceResult? = player.world.rayTraceBlocks(
                    eyeLocation,
                    eyeLocation.direction,
                    maxDistance
                )
                result?.hitBlock != null && (isVersion1_13OrNewer && result.hitBlock?.type?.isAir == true)
            } else {
                val eyeLocation = player.eyeLocation
                val iterator = BlockIterator(
                    player.world,
                    eyeLocation.toVector(),
                    eyeLocation.direction,
                    0.0,
                    maxDistance.toInt()
                )

                if (iterator.hasNext()) iterator.next()

                while (iterator.hasNext()) {
                    val block = iterator.next()
                    if (block.type != Material.AIR) {
                        val distance = eyeLocation.distance(block.location.add(0.5, 0.5, 0.5))
                        if (distance <= maxDistance) {
                            return true
                        }
                        break
                    }
                }
                false
            }
        } catch (e: Exception) {
            plugin?.logger?.warning("Block detection failed: ${e.message}")
            false
        }
    }

    private fun handleClickCheck(player: Player, type: String, clickCount: Int): Boolean {
        val maxAllowed = if (type == "LEFT") config.maxLeftCps else config.maxRightCps

        if (clickCount <= maxAllowed) {
            if (warningCounts[player.uniqueId] ?: 0 > 0) {
                warningCounts[player.uniqueId] = 0
            }
            return false
        }

        val uuid = player.uniqueId
        val newWarnings = (warningCounts[uuid] ?: 0) + 1
        warningCounts[uuid] = newWarnings

        warningSystem?.addWarning(player, 1, config.flag)

        return newWarnings >= config.warnThreshold
    }

    @JvmStatic
    fun reload() {
        checkServerVersion()
        loadConfig()
    }

    fun clearPlayerData(player: Player) {
        val uuid = player.uniqueId
        clickLogs.remove(uuid)
        warningCounts.remove(uuid)
    }

    @JvmStatic
    fun clearAllData() {
        clickLogs.clear()
        warningCounts.clear()
    }

    private data class Config(
        val enabled: Boolean,
        val maxLeftCps: Int,
        val maxRightCps: Int,
        val checkWindow: Int,
        val warnThreshold: Int,
        val blockExemptionDistance: Double,
        val flag: String
    )
}