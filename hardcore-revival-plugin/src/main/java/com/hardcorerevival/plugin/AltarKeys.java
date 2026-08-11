package com.hardcorerevival.plugin;

import org.bukkit.Location;

final class AltarKeys {

    private AltarKeys() {
    }

    static String of(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + ","
                + location.getBlockY() + "," + location.getBlockZ();
    }
}
