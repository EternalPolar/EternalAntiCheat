package eternalpolar.spigot.eternalanticheat.Movement.Fly

import eternalpolar.spigot.eternalanticheat.Data.PlayerMovementData
import eternalpolar.spigot.eternalanticheat.Utils.PlayerWarning
import eternalpolar.spigot.eternalanticheat.Version.VersionBlockHelper
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import java.io.File
import java.util.*
import java.util.ArrayDeque
/**
 *  Eternal Polar 2025/7/27
 */
object AntiFly {
    private val playerData = mutableMapOf<UUID, PlayerMovementData>()
    private val positionHistory = mutableMapOf<UUID, ArrayDeque<Location>>()
    private val groundPositions = mutableMapOf<UUID, Location>()
    private var playerWarning: PlayerWarning? = null
    private lateinit var config: Config
    private var plugin: JavaPlugin? = null
    private const val HISTORY_SIZE = 10
    private const val MAX_VERTICAL_SEARCH = 20
    private const val ANOMALY_DELAY_MS = 5 // 异常延迟判断时间(毫秒)

    @JvmStatic
    fun initialize(plugin: JavaPlugin, warningSystem: PlayerWarning) {
        this.plugin = plugin
        this.playerWarning = warningSystem
        loadConfig()
    }

    private fun loadConfig() {
        val configFile = File(plugin?.dataFolder, "Movement/AntiFly.yml").apply {
            if (!exists()) {
                parentFile.mkdirs()
                plugin?.saveResource("Movement/AntiFly.yml", false)
            }
        }

        val yaml = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(configFile)

        config = Config(
            enable = yaml.getBoolean("enable", true),
            maxAscendSpeed = yaml.getDouble("max-ascend-speed", 0.42),
            maxDescendSpeed = yaml.getDouble("max-descend-speed", 0.56),
            maxHorizontalSpeed = yaml.getDouble("max-horizontal-speed", 0.36),
            minGlideThreshold = yaml.getDouble("min-glide-threshold", 0.98),
            rollbackSeconds = yaml.getDouble("rollback-seconds", 2.0),
            rollbackStrength = yaml.getDouble("rollback-strength", 0.7),
            minFallDistance = yaml.getDouble("min-fall-distance", 2.0),
            fallVelocityThreshold = yaml.getDouble("fall-velocity-threshold", -0.3),
            waterExemption = yaml.getBoolean("water-exemption", true),
            flag = yaml.getString("flag", "Fly") ?: "Fly"
        )
    }

    @JvmStatic
    fun check(player: Player): Boolean {
        if (!config.enable || shouldExempt(player)) {
            playerData[player.uniqueId]?.resetAnomalyTracking()
            return false
        }

        val uuid = player.uniqueId
        val data = playerData.getOrPut(uuid) { PlayerMovementData() }
        val location = player.location
        val velocity = player.velocity
        val isOnGround = isOnGround(location, velocity)
        if (isOnGround) {
            groundPositions[uuid] = location.clone()
        }

        recordPositionHistory(uuid, location)
        val lastLocation = data.lastLocation
        return if (lastLocation != null) {
            updateMovementData(data, lastLocation, location, velocity)
            performEnhancedChecks(data, player)
        } else {
            data.lastLocation = location.clone()
            false
        }
    }

    private fun recordPositionHistory(uuid: UUID, location: Location) {
        positionHistory.getOrPut(uuid) { ArrayDeque(HISTORY_SIZE + 1) }.apply {
            addFirst(location.clone())
            if (size > HISTORY_SIZE) removeLast()
        }
    }

    private fun shouldExempt(player: Player): Boolean {
        return player.gameMode == GameMode.CREATIVE ||
                player.gameMode == GameMode.SPECTATOR ||
                player.isFlying ||
                player.isInsideVehicle ||
                player.isOp ||
                isInWater(player) ||
                isSafeFall(player)
    }

    private fun isSafeFall(player: Player): Boolean {
        return player.fallDistance > config.minFallDistance &&
                player.velocity.y < config.fallVelocityThreshold &&
                !isOnSolidSurface(player.location)
    }

    private fun isInWater(player: Player): Boolean {
        return config.waterExemption &&
                (player.location.block.isLiquid || player.eyeLocation.block.isLiquid)
    }

    private fun isOnSolidSurface(loc: Location): Boolean {
        return VersionBlockHelper.isSolid(getBlockBelow(loc, 0.5).type)
    }

    private fun isOnGround(loc: Location, velocity: Vector): Boolean {
        if (velocity.y == 0.0) return true

        val blockBelow = getBlockBelow(loc, 0.3)
        return when {
            VersionBlockHelper.isSolid(blockBelow.type) -> true
            VersionBlockHelper.isLiquid(blockBelow.type) -> Math.abs(velocity.y) < 0.05
            VersionBlockHelper.isClimbable(blockBelow.type) -> Math.abs(velocity.y) < 0.1
            else -> false
        }
    }

    private fun updateMovementData(
        data: PlayerMovementData,
        from: Location,
        to: Location,
        velocity: Vector
    ) {
        data.setGroundState(isOnGround(to, velocity))
        data.updateMovementData(to, velocity)
    }

    private fun getBlockBelow(location: Location, distance: Double): Block {
        return location.clone().subtract(0.0, distance, 0.0).block
    }

    private fun performEnhancedChecks(data: PlayerMovementData, player: Player): Boolean {
        val verticalSpeed = Math.abs(data.lastDeltaY) * 20
        val horizontalSpeed = Math.hypot(data.lastDeltaX, data.lastDeltaZ) * 20
        val glideRatio = data.getGlideRatio()
        val velocityDiff = Math.abs(verticalSpeed - Math.abs(data.lastVelocity.y) * 20)

        val checks = listOf(
            data.airTicks > 15 && data.lastDeltaY <= 0.0,
            data.lastDeltaY > 0.0 && verticalSpeed > config.maxAscendSpeed,
            data.lastDeltaY < 0.0 && verticalSpeed > config.maxDescendSpeed,
            horizontalSpeed > config.maxHorizontalSpeed && !data.isOnGround(),
            glideRatio > config.minGlideThreshold && !data.isOnGround(),
            data.airTicks > 30 && Math.abs(data.lastDeltaY) < 0.01 && !data.isOnGround(),
            velocityDiff > 5.0
        )

        val isAnomaly = checks.count { it } >= 4

        return if (isAnomaly) {
            val currentTime = System.currentTimeMillis()
            when {
                data.firstAnomalyTime == 0L -> {
                    data.firstAnomalyTime = currentTime
                    false
                }
                currentTime - data.firstAnomalyTime >= ANOMALY_DELAY_MS -> {
                    data.increaseViolationCount()
                    playerWarning?.addWarning(player, 1, config.flag)
                    applyEnhancedRollback(player)
                    data.resetAnomalyTracking()
                    true
                }
                else -> {
                    false
                }
            }
        } else {
            data.resetAnomalyTracking()
            if (data.consecutiveViolations > 0) {
                data.consecutiveViolations--
            }
            false
        }
    }

    private fun applyEnhancedRollback(player: Player) {
        val uuid = player.uniqueId
        val safeLocation = groundPositions[uuid]?.clone()
            ?: findNearestGround(player.location, uuid)
            ?: player.world.spawnLocation

        player.velocity = player.velocity.clone().apply {
            multiply(-config.rollbackStrength * 1.5)
            y = -0.7
        }
        player.teleport(safeLocation.clone().apply {
            yaw = player.location.yaw
            pitch = player.location.pitch
        })

        playerData[uuid]?.apply {
            lastLocation = safeLocation.clone()
            lastVelocity = Vector(0, 0, 0)
            airTicks = 0
            resetViolationCount()
        }
        positionHistory.remove(uuid)
    }

    private fun findNearestGround(current: Location, playerUuid: UUID): Location? {
        (0..MAX_VERTICAL_SEARCH).forEach { yOffset ->
            val checkLoc = current.clone().apply { y -= yOffset.toDouble() }
            if (isSolidGround(checkLoc)) {
                return checkLoc.clone().apply { y += 1.0 }
            }
        }
        return positionHistory[playerUuid]?.firstOrNull { isSolidGround(it) }
    }

    private fun isSolidGround(loc: Location): Boolean {
        val blockBelow = loc.clone().subtract(0.0, 0.5, 0.0).block
        return VersionBlockHelper.isSolid(blockBelow.type) &&
                !VersionBlockHelper.isClimbable(blockBelow.type)
    }

    @JvmStatic
    fun clearPlayerData(player: Player) {
        val uuid = player.uniqueId
        playerData.remove(uuid)
        positionHistory.remove(uuid)
        groundPositions.remove(uuid)
    }

    @JvmStatic
    fun clearAllData() {
        playerData.clear()
        positionHistory.clear()
        groundPositions.clear()
    }

    @JvmStatic
    fun reloadConfig() {
        plugin?.reloadConfig()
        loadConfig()
    }

    data class Config(
        val enable: Boolean,
        val maxAscendSpeed: Double,
        val maxDescendSpeed: Double,
        val maxHorizontalSpeed: Double,
        val minGlideThreshold: Double,
        val rollbackSeconds: Double,
        val rollbackStrength: Double,
        val minFallDistance: Double,
        val fallVelocityThreshold: Double,
        val waterExemption: Boolean,
        val flag: String
    )
}
