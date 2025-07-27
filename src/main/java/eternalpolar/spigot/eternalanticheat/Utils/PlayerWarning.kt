package eternalpolar.spigot.eternalanticheat.Utils

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
*  Eternal Polar 2025/7/27
*/
class PlayerWarning(private val plugin: JavaPlugin) {
    private val playerWarnings = ConcurrentHashMap<UUID, Int>()
    private val lastWarningTimes = ConcurrentHashMap<UUID, Long>()
    private val playerLogEnabled = ConcurrentHashMap<UUID, Boolean>().withDefault { false }
    private lateinit var langConfig: FileConfiguration
    private lateinit var langFile: File
    private lateinit var playerLogFile: File
    private lateinit var playerLogConfig: FileConfiguration

    private var warningThreshold = 3
    private var warningCooldown = 500L
    private var executeCommands = listOf<String>()
    private var decayInterval = 30000L
    private var decayAmount = 1
    private var logToConsole = true
    private var notifyOps = true

    init {
        createLangFile()
        createPlayerLogFile()
        loadConfig()
        loadPlayerLogStates()
        startDecayTask()
    }

    private fun createLangFile() {
        langFile = File(plugin.dataFolder, "lang.yml")
        if (!langFile.exists()) {
            langFile.parentFile.mkdirs()
            plugin.saveResource("lang.yml", false)
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile)

        if (!langConfig.contains("log-toggle-enabled")) {
            langConfig.set("log-toggle-enabled", "&aYour warning logs have been enabled")
        }
        if (!langConfig.contains("log-toggle-disabled")) {
            langConfig.set("log-toggle-disabled", "&cYour warning logs have been disabled")
        }
        saveLang()
    }

    private fun createPlayerLogFile() {
        playerLogFile = File(plugin.dataFolder, "player-logs.yml")
        if (!playerLogFile.exists()) {
            playerLogFile.parentFile.mkdirs()
            playerLogFile.createNewFile()
        }
        playerLogConfig = YamlConfiguration.loadConfiguration(playerLogFile)
    }

    private fun loadPlayerLogStates() {
        playerLogConfig.getKeys(false).forEach { uuidStr ->
            try {
                val uuid = UUID.fromString(uuidStr)
                val state = playerLogConfig.getBoolean(uuidStr)
                playerLogEnabled[uuid] = state
            } catch (e: IllegalArgumentException) {
                plugin.logger.warning("Invalid UUID in player-logs.yml: $uuidStr")
            }
        }
    }

    private fun savePlayerLogState(uuid: UUID, state: Boolean) {
        try {
            playerLogConfig.set(uuid.toString(), state)
            playerLogConfig.save(playerLogFile)
        } catch (e: IOException) {
            plugin.logger.severe("Could not save player log state: ${e.message}")
        }
    }

    fun reloadLang() {
        langConfig = YamlConfiguration.loadConfiguration(langFile)
    }

    fun saveLang() {
        try {
            langConfig.save(langFile)
        } catch (e: IOException) {
            plugin.logger.severe("Could not save lang.yml: ${e.message}")
        }
    }

    fun loadConfig() {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()

        val config = plugin.config
        warningThreshold = config.getInt("Warning.warning-threshold", 3)
        warningCooldown = config.getLong("Warning.warning-cooldown", 500)
        decayInterval = config.getLong("Warning.decay-interval", 30000)
        decayAmount = config.getInt("Warning.decay-amount", 1)
        executeCommands = config.getStringList("Warning.execute-commands")
        logToConsole = config.getBoolean("Warning.log-to-console", true)
        notifyOps = config.getBoolean("Warning.notify-ops", true)

        if (!langConfig.contains("warning-message")) {
            langConfig.set("warning-message", "[AC] %player% triggered %check% (%warnings%/%threshold%)")
        }
        if (!langConfig.contains("punish-message")) {
            langConfig.set("punish-message", "[AC] Player %player% was punished for cheating")
        }
        if (!langConfig.contains("console-log")) {
            langConfig.set("console-log", "[AC] Player %player% triggered %check% (Warnings: %warnings%/%threshold%)")
        }
        saveLang()
    }

    private fun startDecayTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            playerWarnings.keys.forEach { uuid ->
                Bukkit.getPlayer(uuid)?.let { player ->
                    decreaseWarning(player, decayAmount, true)
                }
            }
        }, decayInterval / 50, decayInterval / 50)
    }

    fun togglePlayerLog(player: Player): Boolean {
        val uuid = player.uniqueId
        val currentState = playerLogEnabled.getOrDefault(uuid, false)
        val newState = !currentState
        playerLogEnabled[uuid] = newState
        savePlayerLogState(uuid, newState)

        val message = if (newState) {
            langConfig.getString("log-toggle-enabled", "&aYour warning logs have been enabled")
        } else {
            langConfig.getString("log-toggle-disabled", "&cYour warning logs have been disabled")
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message ?: ""))

        return newState
    }

    fun addWarning(player: Player, amount: Int, name: String): Int {
        if (System.currentTimeMillis() - lastWarningTimes.getOrDefault(player.uniqueId, 0L) < warningCooldown) {
            return getWarning(player)
        }

        val uuid = player.uniqueId
        val newAmount = playerWarnings.getOrDefault(uuid, 0) + amount
        playerWarnings[uuid] = newAmount
        lastWarningTimes[uuid] = System.currentTimeMillis()

        sendWarning(player, newAmount, name)

        if (newAmount >= warningThreshold) {
            executePunish(player)
            resetWarning(player)
        }

        return newAmount
    }

    private fun sendWarning(player: Player, warnings: Int, name: String) {
        if (logToConsole) {
            val consoleMsg = langConfig.getString("console-log", "")
                ?.replace("%player%", player.name)
                ?.replace("%warnings%", warnings.toString())
                ?.replace("%threshold%", warningThreshold.toString())
                ?.replace("%check%", name)
            consoleMsg?.let { plugin.logger.info(it) }
        }

        if (notifyOps) {
            val message = langConfig.getString("warning-message", "")
                ?.replace("%player%", player.name)
                ?.replace("%warnings%", warnings.toString())
                ?.replace("%threshold%", warningThreshold.toString())
                ?.replace("%check%", name)
                ?: return

            Bukkit.getOnlinePlayers().filter { it.isOp }.forEach {
                it.sendMessage(ChatColor.translateAlternateColorCodes('&', message))
            }
        }

        if (playerLogEnabled.getOrDefault(player.uniqueId, false)) {
            val playerMsg = langConfig.getString("warning-message", "")
                ?.replace("%player%", player.name)
                ?.replace("%warnings%", warnings.toString())
                ?.replace("%threshold%", warningThreshold.toString())
                ?.replace("%check%", name)
                ?: return

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', playerMsg))
        }
    }

    private fun executePunish(player: Player) {
        val punishMsg = langConfig.getString("punish-message", "")
            ?.replace("%player%", player.name)
            ?: return

        if (notifyOps) {
            Bukkit.getOnlinePlayers().filter { it.isOp }.forEach {
                it.sendMessage(ChatColor.translateAlternateColorCodes('&', punishMsg))
            }
        }

        if (logToConsole) {
            plugin.logger.info(punishMsg)
        }

        executeCommands.forEach { cmd ->
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                ColorCode.color(cmd.replace("%player%", player.name))
            )
        }
    }

    fun decreaseWarning(player: Player, amount: Int, silent: Boolean = false): Int {
        val uuid = player.uniqueId
        val current = playerWarnings.getOrDefault(uuid, 0)
        if (current <= 0) return 0

        val newAmount = maxOf(0, current - amount)
        playerWarnings[uuid] = newAmount
        return newAmount
    }

    fun getWarning(player: Player): Int = playerWarnings.getOrDefault(player.uniqueId, 0)

    fun resetWarning(player: Player) {
        playerWarnings.remove(player.uniqueId)
        lastWarningTimes.remove(player.uniqueId)
    }

    fun clearAllWarnings() {
        playerWarnings.clear()
        lastWarningTimes.clear()
    }

    fun isPlayerLogEnabled(player: Player): Boolean {
        return playerLogEnabled.getOrDefault(player.uniqueId, false)
    }
}