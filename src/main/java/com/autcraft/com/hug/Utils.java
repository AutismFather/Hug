package com.autcraft.com.hug;

import java.io.IOException;
import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Utils {

    private static final HashMap<String, Long> hug_cds = new HashMap<String, Long>();
    private static final HashMap<String, Long> grouphug_cds = new HashMap<String, Long>();
    private static final HashMap<String, Long> pride_cds = new HashMap<String, Long>();

    private static File playersFile;

    private static String messagePrefix;
    private static HugPlugin plugin;

    public static void SetPlugin(HugPlugin plugin) {
        Utils.plugin = plugin;

        playersFile = new File(plugin.getDataFolder(), "players.yml");

        reloadConfig();
    }

    public static HugPlugin getPlugin() {
        return plugin;
    }

    public static File getPlayersFile() {
        return playersFile;
    }

    public static void reloadConfig() {
        // If not already created, make our players.yml file
        if (!playersFile.exists()) {
            try {
                playersFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create players.yml");
            }
        }

        messagePrefix = plugin.getConfig().getString("prefix");
    }

    public static String formatMessage(String message, boolean ok) {
        if (ok) return formatMessage(message, ChatColor.GREEN);
        else return formatMessage(message, ChatColor.RED);
    }

    public static String formatMessage(String message, ChatColor color) {
        return color + formatMessage(message);
    }

    public static String formatMessage(String message) {
        return messagePrefix + message;
    }

    public static void log(String message) {
        Bukkit.getLogger().info(messagePrefix + message);
    }

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static long cooldown(Player player, int seconds, String type) {
        long timer = 0;
        long currentTime = System.currentTimeMillis();

        switch (type) {
            case "hug":
                if (!hug_cds.containsKey(player.getName())) {
                    hug_cds.put(player.getName(), currentTime);
                } else if (currentTime - seconds >= hug_cds.get(player.getName())) {
                    hug_cds.put(player.getName(), currentTime);
                } else {
                    timer = Math.toIntExact(seconds - (currentTime - hug_cds.get(player.getName()))) / 1000;
                }
                break;
            
            case "grouphug":
                // Either add a new timer to the index, or check the time left on the timer
                if (!grouphug_cds.containsKey(player.getName())) {
                    grouphug_cds.put(player.getName(), currentTime);
                } else if (currentTime == grouphug_cds.get(player.getName()) || currentTime - seconds >= grouphug_cds.get(player.getName())) {
                    grouphug_cds.put(player.getName(), currentTime);
                } else {
                    timer = Math.toIntExact(seconds - (currentTime - grouphug_cds.get(player.getName()))) / 1000;
                }
                break;

            case "pride":
                // Either add a new timer to the index, or check the time left on the timer
                if (!pride_cds.containsKey(player.getName())) {
                    pride_cds.put(player.getName(), currentTime);
                } else if (currentTime == pride_cds.get(player.getName()) || currentTime - seconds >= pride_cds.get(player.getName())) {
                    pride_cds.put(player.getName(), currentTime);
                } else {
                    timer = Math.toIntExact(seconds - (currentTime - pride_cds.get(player.getName()))) / 1000;
                }
                break;
        }

        return timer;
    }

}