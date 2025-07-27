package eternalpolar.spigot.eternalanticheat.Utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

       /**
        *  Eternal Polar 2025/7/27
        */
public class ColorCode {

    public static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> list) {
        if (list == null) return new ArrayList<>();

        List<String> coloredList = new ArrayList<>();
        for (String text : list) {
            coloredList.add(color(text));
        }
        return coloredList;
    }

    public static String stripColor(String text) {
        return text == null ? "" : ChatColor.stripColor(text);
    }

    public static List<String> stripColor(List<String> list) {
        if (list == null) return new ArrayList<>();

        List<String> strippedList = new ArrayList<>();
        for (String text : list) {
            strippedList.add(stripColor(text));
        }
        return strippedList;
    }
}