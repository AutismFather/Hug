package com.autcraft.com.hug;

import org.bukkit.ChatColor;

public class colorUtils {
    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
