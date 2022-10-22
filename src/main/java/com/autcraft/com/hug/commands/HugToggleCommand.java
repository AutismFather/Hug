package com.autcraft.com.hug.commands;

import com.autcraft.com.hug.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.UUID;

public class HugToggleCommand implements CommandExecutor {

    /*
        Turns on and off your acceptance of receiving hugs.
        on = you are willing to receive a hug
        off = you do not wish to receive hugs
    */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Be able to take this away from people if necessary.
        if (sender.hasPermission("hug.hugtoggle") == false) {
            sender.sendMessage(Utils.formatMessage("Insufficient permissions for hugtoggle.", false));
            return true;
        }

        // Open the file
        YamlConfiguration playersYml = YamlConfiguration.loadConfiguration(Utils.getPlayersFile());

        // Get player's UUID
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        // Defaults
        String toggle = "on";
        String toggle_hugs = "on";
        String toggle_grouphugs = "on";
        String toggle_pride = "on";

        if (args.length == 0) {
            player.sendMessage(ChatColor.AQUA + "Hugtoggle Usage:");
            player.sendMessage(ChatColor.AQUA + "/hugtoggle hugs on/off");
            player.sendMessage(ChatColor.AQUA + "/hugtoggle grouphugs on/off");
            player.sendMessage(ChatColor.AQUA + "/hugtoggle pride on/off");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "hugs":
                // Set hugs to on
                if (args.length > 1 && args[1].equalsIgnoreCase("on")) {
                    toggle_hugs = "on";
                } else if (args.length > 1 && args[1].equalsIgnoreCase("off")) {
                    toggle_hugs = "off";
                } else {
                    // Get the current toggle and flip it.
                    toggle = playersYml.getString(uuid + ".hugs");
                    if (toggle == null || toggle == "on") {
                        toggle_hugs = "off";
                    }
                }
                // Set yml value
                playersYml.set(uuid + ".hugs", toggle_hugs);
                break;
            
            case "grouphugs":
                // Set grouphugs to on
                if (args.length > 1 && args[1].equalsIgnoreCase("on")) {
                    toggle_grouphugs = "on";
                } else if (args.length > 1 && args[1].equalsIgnoreCase("off")) {
                    toggle_grouphugs = "off";
                } else {
                    // Get the current toggle and flip it.
                    toggle = playersYml.getString(uuid + ".grouphugs");
                    if (toggle == null || toggle.equalsIgnoreCase("on")) {
                        toggle_grouphugs = "off";
                    }
                }
                // Set yml value
                playersYml.set(uuid + ".grouphugs", toggle_grouphugs);
                break;
            
            case "pride":
                // Set hugs to on
                if (args.length > 1 && args[1].equalsIgnoreCase("on")) {
                    toggle_grouphugs = "on";
                } else if (args.length > 1 && args[1].equalsIgnoreCase("off")) {
                    toggle_grouphugs = "off";
                } else {
                    // Get the current toggle and flip it.
                    toggle = playersYml.getString(uuid + ".pride");
                    if (toggle == null || toggle.equalsIgnoreCase("on")) {
                        toggle_grouphugs = "off";
                    }
                }
                // Set yml value
                playersYml.set(uuid + ".pride", toggle_grouphugs);
                break;

            default:
                player.sendMessage(ChatColor.AQUA + "Hugtoggle Usage:");
                player.sendMessage(ChatColor.AQUA + "/hugtoggle hugs on/off");
                player.sendMessage(ChatColor.AQUA + "/hugtoggle grouphugs on/off");
                player.sendMessage(ChatColor.AQUA + "/hugtoggle pride on/off");
                return true;
        }

        // Save the file
        try {
            playersYml.save(Utils.getPlayersFile());
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe("Could not save players.yml");
        }

        // Now for the final message, this is mostly just to avoid showing "null" as a result if it has not yet been set
        toggle_hugs = playersYml.getString(uuid + ".hugs");
        if (toggle_hugs == null) toggle_hugs = "on";

        toggle_grouphugs = playersYml.getString(uuid + ".grouphugs");
        if (toggle_grouphugs == null) toggle_grouphugs = "on";

        toggle_pride = playersYml.getString(uuid + ".pride");
        if (toggle_pride == null) toggle_pride = "on";

        // Return a verification message to the player
        sender.sendMessage(ChatColor.GREEN + "Auto accept hugs? " + toggle_hugs);
        sender.sendMessage(ChatColor.GREEN + "Auto accept group hugs? " + toggle_grouphugs);
        sender.sendMessage(ChatColor.GREEN + "Auto accept pride hearts? " + toggle_pride);
        return true;
    }

}