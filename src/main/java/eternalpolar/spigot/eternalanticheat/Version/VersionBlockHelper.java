package eternalpolar.spigot.eternalanticheat.Version;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Arrays;
import java.util.List;

public class VersionBlockHelper {
    /**
     *  Eternal Polar 2025/7/27
     */
    public static Material WATER;
    public static Material STATIONARY_WATER;
    public static Material LAVA;
    public static Material STATIONARY_LAVA;
    public static Material ICE;
    public static Material PACKED_ICE;
    public static Material BLUE_ICE;
    public static Material FROSTED_ICE;
    public static Material SOUL_SAND;
    public static Material SOUL_SOIL;
    public static Material LADDER;
    public static Material VINE;
    public static Material WEB;
    public static Material COBWEB;
    public static Material AIR;
    public static Material CAVE_VINES;
    public static Material TWISTING_VINES;
    public static Material WEEPING_VINES;
    public static Material SLAB;
    public static Material STAIRS;
    public static Material SLIME_BLOCK;
    public static Material HONEY_BLOCK;
    public static Material SCAFFOLDING;
    public static Material BAMBOO_SCAFFOLDING;
    public static Material MUD;
    public static Material MANGROVE_ROOTS;
    private static String serverVersion;
    private static int majorVersion;
    private static final List<String> LEVITATION_NAMES = Arrays.asList(
            "LEVITATION",
            "SLOW_FALLING"
    );

    public static void initialize() {
        detectServerVersion();

        if (majorVersion < 13) {
            initializeLegacy();
        } else if (majorVersion < 17) {
            initialize1_13To16();
        } else if (majorVersion < 19) {
            initialize1_17To18();
        } else if (majorVersion < 21) {
            initialize1_19To20();
        } else {
            initialize1_21Plus();
        }
    }

    private static void detectServerVersion() {
        try {
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            serverVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
            String[] parts = serverVersion.split("_");
            majorVersion = parts.length >= 2 ? Integer.parseInt(parts[1]) : -1;
        } catch (Exception e) {
            serverVersion = "unknown";
            majorVersion = -1;
        }
    }

    private static void initializeLegacy() {
        WATER = getMaterial("WATER", Material.AIR);
        STATIONARY_WATER = getMaterial("STATIONARY_WATER", Material.AIR);
        LAVA = getMaterial("LAVA", Material.AIR);
        STATIONARY_LAVA = getMaterial("STATIONARY_LAVA", Material.AIR);
        ICE = getMaterial("ICE", Material.AIR);
        PACKED_ICE = getMaterial("PACKED_ICE", Material.AIR);
        SOUL_SAND = getMaterial("SOUL_SAND", Material.AIR);
        LADDER = getMaterial("LADDER", Material.AIR);
        VINE = getMaterial("VINE", Material.AIR);
        WEB = getMaterial("WEB", Material.AIR);
        COBWEB = getMaterial("WEB", Material.AIR);
        AIR = getMaterial("AIR", Material.AIR);
        SLAB = getMaterial("STEP", Material.AIR);
        STAIRS = getMaterial("WOOD_STAIRS", Material.AIR);
        SLIME_BLOCK = getMaterial("SLIME_BLOCK", Material.AIR);
        SCAFFOLDING = getMaterial("AIR", Material.AIR);
        BAMBOO_SCAFFOLDING = getMaterial("AIR", Material.AIR);
    }

    private static void initialize1_13To16() {
        WATER = getMaterial("WATER", Material.AIR);
        STATIONARY_WATER = getMaterial("WATER", Material.AIR);
        LAVA = getMaterial("LAVA", Material.AIR);
        STATIONARY_LAVA = getMaterial("LAVA", Material.AIR);
        ICE = getMaterial("ICE", Material.AIR);
        PACKED_ICE = getMaterial("PACKED_ICE", Material.AIR);
        BLUE_ICE = getMaterial("BLUE_ICE", Material.AIR);
        SOUL_SAND = getMaterial("SOUL_SAND", Material.AIR);
        SOUL_SOIL = getMaterial("SOUL_SOIL", Material.AIR);
        LADDER = getMaterial("LADDER", Material.AIR);
        VINE = getMaterial("VINE", Material.AIR);
        WEB = getMaterial("COBWEB", Material.AIR);
        COBWEB = getMaterial("COBWEB", Material.AIR);
        AIR = getMaterial("AIR", Material.AIR);
        SLAB = getMaterial("STONE_SLAB", Material.AIR);
        STAIRS = getMaterial("OAK_STAIRS", Material.AIR);
        SLIME_BLOCK = getMaterial("SLIME_BLOCK", Material.AIR);
        SCAFFOLDING = getMaterial("SCAFFOLDING", Material.AIR);
        BAMBOO_SCAFFOLDING = getMaterial("BAMBOO_SCAFFOLDING", Material.AIR);
    }

    private static void initialize1_17To18() {
        initialize1_13To16();
        CAVE_VINES = getMaterial("CAVE_VINES", Material.AIR);
        TWISTING_VINES = getMaterial("TWISTING_VINES", Material.AIR);
        WEEPING_VINES = getMaterial("WEEPING_VINES", Material.AIR);
        FROSTED_ICE = getMaterial("FROSTED_ICE", Material.AIR);
    }

    private static void initialize1_19To20() {
        initialize1_17To18();
        MUD = getMaterial("MUD", Material.AIR);
        MANGROVE_ROOTS = getMaterial("MANGROVE_ROOTS", Material.AIR);
        HONEY_BLOCK = getMaterial("HONEY_BLOCK", Material.AIR);
    }

    private static void initialize1_21Plus() {
        initialize1_19To20();
    }

    private static Material getMaterial(String name, Material fallback) {
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    public static String getServerVersion() {
        return serverVersion;
    }

    public static int getMajorVersion() {
        return majorVersion;
    }

    // ===== 方块检测方法 =====

    public static boolean isSolid(Material material) {
        if (material == null || material == AIR) {
            return false;
        }
        return !isLiquid(material) &&
                !isClimbable(material) &&
                !isWeb(material) &&
                !material.name().endsWith("_SAPLING") &&
                !material.name().endsWith("_SIGN");
    }

    public static boolean isClimbable(Material material) {
        return isLadder(material) ||
                isVine(material) ||
                material == CAVE_VINES ||
                material == TWISTING_VINES ||
                material == WEEPING_VINES;
    }

    public static boolean isLadder(Material material) {
        return material == LADDER || material.name().endsWith("_LADDER");
    }

    public static boolean isVine(Material material) {
        return material == VINE ||
                material == CAVE_VINES ||
                material == TWISTING_VINES ||
                material == WEEPING_VINES;
    }

    public static boolean isSlab(Material material) {
        return material == SLAB || material.name().endsWith("_SLAB");
    }

    public static boolean isStair(Material material) {
        return material == STAIRS || material.name().endsWith("_STAIRS");
    }

    public static boolean isWater(Material material) {
        return material == WATER || material == STATIONARY_WATER;
    }

    public static boolean isLava(Material material) {
        return material == LAVA || material == STATIONARY_LAVA;
    }

    public static boolean isLiquid(Material material) {
        return isWater(material) || isLava(material);
    }

    public static boolean isIce(Material material) {
        return material == ICE ||
                material == PACKED_ICE ||
                material == BLUE_ICE ||
                material == FROSTED_ICE;
    }

    public static boolean isWeb(Material material) {
        return material == WEB || material == COBWEB;
    }

    public static boolean isSlimeBlock(Material material) {
        return material == SLIME_BLOCK;
    }

    public static boolean isHoneyBlock(Material material) {
        return material == HONEY_BLOCK;
    }

    public static boolean isScaffolding(Material material) {
        return material == SCAFFOLDING ||
                material == BAMBOO_SCAFFOLDING ||
                material.name().endsWith("_SCAFFOLDING");
    }

    public static boolean slowsMovement(Material material) {
        return material == SOUL_SAND || material == SOUL_SOIL || material == MUD;
    }

    public static boolean isPassable(Material material) {
        return material == AIR || isLiquid(material) || isClimbable(material);
    }

    public static boolean isFullBlock(Material material) {
        return isSolid(material) && !isSlab(material) && !isStair(material);
    }

    public static boolean isSolid(Block block) {
        return isSolid(block.getType());
    }

    // ===== 药水效果检测 =====

    public static boolean hasLevitationEffect(Player player) {
        try {
            if (getMajorVersion() >= 9) {
                PotionEffectType levitation = PotionEffectType.LEVITATION;
                return levitation != null && player.hasPotionEffect(levitation);
            } else {
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    String typeName = effect.getType().getName().toUpperCase();
                    if (typeName.contains("LEVITATION")) {
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static int getLevitationAmplifier(Player player) {
        try {
            if (getMajorVersion() >= 9) {
                PotionEffect effect = player.getPotionEffect(PotionEffectType.LEVITATION);
                return effect != null ? effect.getAmplifier() : -1;
            } else {
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    if (effect.getType().getName().toUpperCase().contains("LEVITATION")) {
                        return effect.getAmplifier();
                    }
                }
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    public static boolean hasSlowFalling(Player player) {
        try {
            if (getMajorVersion() >= 13) {
                PotionEffectType slowFalling = PotionEffectType.SLOW_FALLING;
                return slowFalling != null && player.hasPotionEffect(slowFalling);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isBubbleColumn(Material material) {
        if (!(getMajorVersion() >= (13))) return false;
        try {
            return material == Material.BUBBLE_COLUMN;
        } catch (Exception e) {
            return false;
        }
    }
}