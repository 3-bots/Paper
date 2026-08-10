package com.simplehomes.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Hardcoded recovery data for specific players whose homes from the
 * previous same-named plugin weren't picked up by HomeManager's automatic
 * legacy-format migration. Sourced directly from the playerdata files the
 * server owner provided. Only ever consulted as a last resort, when a
 * player's file yields no homes through the normal paths.
 */
final class LegacyHomeSeed {

    private LegacyHomeSeed() {
    }

    static Map<String, Location> forPlayer(UUID uuid) {
        return switch (uuid.toString()) {
            case "00000000-0000-0000-0009-01f11d04d1bd" -> rd0ai0();
            case "00000000-0000-0000-0009-01f2ada4f1fc" -> mrkumi1212();
            case "00000000-0000-0000-0009-01fd08fb47e7" -> ykprim4025();
            default -> Map.of();
        };
    }

    private static Map<String, Location> rd0ai0() {
        Map<String, Location> homes = new LinkedHashMap<>();
        put(homes, SimpleHomesPlugin.DEFAULT_HOME_NAME, "minecraft:overworld", 94.73426, 127.00000000000001, 67.3366, 80.03754f, 38.28f);
        put(homes, "b", "minecraft:the_nether", -169.98169, 62.0, -154.67369, -24.286285f, 29.440964f);
        put(homes, "nb", "minecraft:the_nether", 136.37456, 6.0, 100.01481, -112.78878f, -2.4335175f);
        put(homes, "blaze", "minecraft:the_nether", -218.30001001192093, 63.0, -379.9397, 93.58545f, 34.813385f);
        put(homes, "v", "minecraft:overworld", -1636.8164, 71.0, -182.03217, 26.991394f, 29.111206f);
        put(homes, "slime", "minecraft:overworld", 685.46826, -4.0, -996.14325, 14.752563f, 36.379364f);
        return homes;
    }

    private static Map<String, Location> mrkumi1212() {
        Map<String, Location> homes = new LinkedHashMap<>();
        put(homes, SimpleHomesPlugin.DEFAULT_HOME_NAME, "minecraft:overworld", 88.85399, 127.00000000000001, 68.14235, -73.91055f, 14.156189f);
        put(homes, "t", "minecraft:the_nether", -218.30001001192093, 63.0, -379.78235, -26.443909f, 20.84906f);
        put(homes, "f", "minecraft:overworld", -2543.300010011921, 50.0, -915.3000100119209, 97.616455f, 24.874237f);
        put(homes, "n", "minecraft:the_nether", -183.30001001192093, 65.0, -320.69263, -12.39209f, -13.013596f);
        return homes;
    }

    private static Map<String, Location> ykprim4025() {
        Map<String, Location> homes = new LinkedHashMap<>();
        put(homes, SimpleHomesPlugin.DEFAULT_HOME_NAME, "minecraft:overworld", 101.66339, 126.0, 59.121586, -4.5146637f, 27.037033f);
        put(homes, "1", "minecraft:overworld", 153.69998998807907, -53.0, 438.3000100119209, 95.00195f, 25.93721f);
        put(homes, "2", "minecraft:overworld", 129.78085, -31.0, -25.556046, -117.7322f, 29.90683f);
        put(homes, "5", "minecraft:overworld", 498.4655, -11.0, 110.71341, -91.56543f, 18.166794f);
        put(homes, "e", "minecraft:overworld", -464.29956, 63.0, 528.96497, 33.29544f, 27.470428f);
        put(homes, "n", "minecraft:the_nether", -218.30001001192093, 63.0, -378.3000100119209, 138.82483f, 32.747086f);
        return homes;
    }

    private static void put(Map<String, Location> homes, String name, String worldKey,
            double x, double y, double z, float yaw, float pitch) {
        World world = Bukkit.getWorld(NamespacedKey.fromString(worldKey));
        if (world == null) {
            return;
        }
        homes.put(name, new Location(world, x, y, z, yaw, pitch));
    }
}
