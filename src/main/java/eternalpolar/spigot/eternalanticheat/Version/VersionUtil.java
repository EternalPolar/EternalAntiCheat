package eternalpolar.spigot.eternalanticheat.Version;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Arrays;
import java.util.List;

public class VersionUtil {
    /**
     *  Eternal Polar 2025/7/27
     */
    private static final int MAJOR_VERSION;
    private static final int MINOR_VERSION;
    private static final List<String> LEVITATION_EFFECT_NAMES = Arrays.asList(
            "LEVITATION",
            "SLOW_FALLING"
    );

    static {
        String version = Bukkit.getBukkitVersion();
        String[] parts = version.split("\\.");
        MAJOR_VERSION = Integer.parseInt(parts[0]);
        MINOR_VERSION = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
    }

    public static boolean isVersionBelow(int major) {
        return MAJOR_VERSION < major;
    }

    public static boolean isVersionBelow(int major, int minor) {
        return MAJOR_VERSION < major || (MAJOR_VERSION == major && MINOR_VERSION < minor);
    }

    public static boolean isVersionAtLeast(int major) {
        return MAJOR_VERSION >= major;
    }

    public static boolean isVersionAtLeast(int major, int minor) {
        return MAJOR_VERSION > major || (MAJOR_VERSION == major && MINOR_VERSION >= minor);
    }

    public static int getMajorVersion() {
        return MAJOR_VERSION;
    }

    public static int getMinorVersion() {
        return MINOR_VERSION;
    }

    public static boolean hasLevitationEffect(Player player) {
        try {
            if (isVersionAtLeast(9)) {
                PotionEffectType levitation = PotionEffectType.getByName("LEVITATION");
                return levitation != null && player.hasPotionEffect(levitation);
            } else {
                return hasLegacyEffect(player, "LEVITATION");
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasSlowFallingEffect(Player player) {
        try {
            if (isVersionAtLeast(13)) {
                PotionEffectType slowFalling = PotionEffectType.getByName("SLOW_FALLING");
                return slowFalling != null && player.hasPotionEffect(slowFalling);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasAnyFlyingEffect(Player player) {
        try {
            if (isVersionAtLeast(9)) {
                return hasLevitationEffect(player) || hasSlowFallingEffect(player);
            } else {
                for (String effectName : LEVITATION_EFFECT_NAMES) {
                    if (hasLegacyEffect(player, effectName)) {
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasLegacyEffect(Player player, String effectName) {
        try {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getType().getName().equalsIgnoreCase(effectName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static int getEffectAmplifier(Player player, String effectName) {
        try {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getType().getName().equalsIgnoreCase(effectName)) {
                    return effect.getAmplifier();
                }
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public static int getRemainingEffectTicks(Player player, String effectName) {
        try {
            if (isVersionAtLeast(9)) {
                PotionEffectType type = PotionEffectType.getByName(effectName);
                if (type != null) {
                    PotionEffect effect = player.getPotionEffect(type);
                    return effect != null ? effect.getDuration() : 0;
                }
            } else {
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    if (effect.getType().getName().equalsIgnoreCase(effectName)) {
                        return effect.getDuration();
                    }
                }
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public static PotionEffectType getPotionEffectTypeSafe(String name) {
        try {
            return PotionEffectType.getByName(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isGliding(Player player) {
        try {
            if (isVersionAtLeast(9)) {
                return player.isGliding();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}