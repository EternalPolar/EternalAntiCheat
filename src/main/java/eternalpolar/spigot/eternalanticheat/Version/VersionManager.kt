package eternalpolar.spigot.eternalanticheat.Version

import org.bukkit.Bukkit

object VersionManager {
    /**
     *  Eternal Polar 2025/7/27
     */
    private var serverVersion = 0
    private var fullVersion = "未知"
    
    fun getServerVersion(): Int {
        if (serverVersion == 0) {
            detectServerVersion()
        }
        return serverVersion
    }

    fun getFullVersion(): String {
        if (serverVersion == 0) {
            detectServerVersion()
        }
        return fullVersion
    }

    private fun detectServerVersion() {
        try {
            fullVersion = Bukkit.getServer().javaClass.getPackage().name.split("\\.")[3]

            val parts = fullVersion.split("_")
            if (parts.size >= 2) {
                serverVersion = parts[1].toInt()
            } else {
                serverVersion = -1
            }
        } catch (e: Exception) {
            fullVersion = "未知"
            serverVersion = -1
        }
    }

    fun isBefore(version: Int): Boolean {
        return getServerVersion() < version
    }

    fun isAfter(version: Int): Boolean {
        return getServerVersion() >= version
    }

    fun isBetween(min: Int, max: Int): Boolean {
        val ver = getServerVersion()
        return ver in min..max
    }

    fun reset() {
        serverVersion = 0
        fullVersion = "未知"
    }
}