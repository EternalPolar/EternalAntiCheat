package eternalpolar.spigot.eternalanticheat;


import eternalpolar.spigot.eternalanticheat.Commands.MainCommand;
import eternalpolar.spigot.eternalanticheat.Listener.MoveCheck;
import eternalpolar.spigot.eternalanticheat.Listener.PlayerCheck;
import eternalpolar.spigot.eternalanticheat.Movement.Fly.AntiFly;
import eternalpolar.spigot.eternalanticheat.Player.Click.AntiAutoClick;
import eternalpolar.spigot.eternalanticheat.Utils.Metrics;
import eternalpolar.spigot.eternalanticheat.Utils.PlayerWarning;
import eternalpolar.spigot.eternalanticheat.Utils.Update;
import eternalpolar.spigot.eternalanticheat.Version.VersionBlockHelper;
import org.bukkit.plugin.java.JavaPlugin;

public final class EternalAntiCheat extends JavaPlugin {
    private MoveCheck moveCheck;
    private PlayerCheck playerCheck;
    private PlayerWarning playerWarning;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("");
        getLogger().info("| Eternal AntiCheat is enabled");
        getLogger().info("|     Version: " + getDescription().getVersion());
        getLogger().info("| Made by Eternal Polar, 3U8655");
        getLogger().info("");
        this.playerWarning = new PlayerWarning(this);
        AntiFly.INSTANCE.initialize(this, playerWarning);
        getLogger().info("| ==》 Loading AntiFly module");
        AntiAutoClick.init(this, playerWarning);
        getLogger().info("| ==》 Loading AntiAutoClick module ==》");
        this.moveCheck = new MoveCheck(this);
        this.playerCheck = new PlayerCheck(this, playerWarning);
        VersionBlockHelper.initialize();
        getServer().getPluginManager().registerEvents(new Update(this), this);
        initializeMetrics();
        getLogger().info("| ==》 EternalAntiCheat has been Enabled! 《==");
        getCommand("eternalanticheat").setExecutor(new MainCommand(this));
    }

    @Override
    public void onDisable() {
    }

    // 新增方法：获取警告系统实例
    public PlayerWarning getWarningSystem() {
        return playerWarning;
    }

    private void initializeMetrics() {
        int pluginId = 26653; // 替换为你的bStats ID
        new Metrics(this, pluginId);
    }
}