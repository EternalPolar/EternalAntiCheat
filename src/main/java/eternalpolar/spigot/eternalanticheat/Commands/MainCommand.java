package eternalpolar.spigot.eternalanticheat.Commands;

import eternalpolar.spigot.eternalanticheat.Listener.MoveCheck;
import eternalpolar.spigot.eternalanticheat.Listener.PlayerCheck;
import eternalpolar.spigot.eternalanticheat.Utils.ColorCode;
import eternalpolar.spigot.eternalanticheat.Utils.PlayerWarning;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
/**
 *  Eternal Polar 2025/7/27
 */
public class MainCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private FileConfiguration langConfig;
    private File langFile;

    // 可配置的消息
    private String noPermissionMsg;
    private String playerOnlyMsg;
    private String openUsageMsg;
    private String reloadSuccessMsg;

    public MainCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        createLangFile();
        loadConfigMessages();
    }

    private void createLangFile() {
        langFile = new File(plugin.getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            plugin.saveResource("lang.yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    private void reloadLang() {
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    private void saveLang() {
        try {
            langConfig.save(langFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save lang.yml: " + e.getMessage());
        }
    }

    private void loadConfigMessages() {
        // 确保语言文件中有默认消息
        if (!langConfig.contains("no-permission")) {
            langConfig.set("no-permission", "&cYou don't have permission to use this command");
        }
        if (!langConfig.contains("player-only")) {
            langConfig.set("player-only", "&cOnly players can execute this command");
        }
        if (!langConfig.contains("open-usage")) {
            langConfig.set("open-usage", "&cUsage: /em open <room name>");
        }
        if (!langConfig.contains("reload-success")) {
            langConfig.set("reload-success", "&aConfiguration reloaded successfully");
        }
        saveLang();

        // 加载消息
        noPermissionMsg = langConfig.getString("no-permission");
        playerOnlyMsg = langConfig.getString("player-only");
        openUsageMsg = langConfig.getString("open-usage");
        reloadSuccessMsg = langConfig.getString("reload-success");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("eternalanticheat.admin")) {
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "     &bEternal&3AntiCheat&f v" + plugin.getDescription().getVersion()));
                    sender.sendMessage("   Powered by Eternal_Polar ,3U8655");
                    sender.sendMessage("");
                    return true;
                }
                return handleReload(sender);
            case "verbose":
                if (!sender.hasPermission("eternalanticheat.verbose")) {
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "     &bEternal&3AntiCheat&f v" + plugin.getDescription().getVersion()));
                    sender.sendMessage("   Powered by Eternal_Polar ,3U8655");
                    sender.sendMessage("");
                    return true;
                }
                return new PlayerWarning(plugin).togglePlayerLog((Player) sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("eternalanticheat.admin")) {
            sender.sendMessage(ColorCode.color(noPermissionMsg));
            return true;
        }

        plugin.reloadConfig();
        new MoveCheck(plugin).reloadConfig();
        new PlayerCheck(plugin, new PlayerWarning(plugin)).reload();
        reloadLang();
        loadConfigMessages();

        sender.sendMessage(ColorCode.color(reloadSuccessMsg));
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        if (!sender.hasPermission("eternalanticheat.admin")) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "     &bEternal&3AntiCheat&f v" + plugin.getDescription().getVersion()));
            sender.sendMessage("   Powered by Eternal_Polar ,3U8655");
            sender.sendMessage("");
            return;
        }

        sender.sendMessage("");
        sender.sendMessage( ChatColor.translateAlternateColorCodes('&', "&bEternal&3AntiCheat&f v&c" + plugin.getDescription().getVersion()));
        sender.sendMessage("");
        sender.sendMessage("/eac reload");
        sender.sendMessage(ChatColor.GRAY + "Reload plugin configuration");
        sender.sendMessage("");
        sender.sendMessage("/eac verbose");
        sender.sendMessage(ChatColor.GRAY + "Show the warn log");
        sender.sendMessage("");
        sender.sendMessage("Created by EternalPolar");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "Started · 2025/7/25");
        sender.sendMessage("");
    }

    public void register() {
        plugin.getCommand("eternalanticheat").setExecutor(this);
    }
}