package com.autcraft.com.hug.commands;

import com.autcraft.com.hug.Utils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PrideCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Be able to take this away from people if necessary.
        if (sender.hasPermission("hug.pride") == false) {
            sender.sendMessage(Utils.formatMessage("Insufficient permissions for /pride command", false));
            return true;
        }

        // The person sending the command.
        Player player = (Player) sender;

        if (!player.hasPermission("hug.cooldown_bypass")) {
            int pride_timer = Utils.getPlugin().getConfig().getInt("pride_timer");

            long timer = Utils.cooldown(player, pride_timer * 1000, "pride");
            if (timer > 0) {
                int minutes = Math.toIntExact(timer / 60); // convert seconds (saved in "time") to minutes
                int seconds = Math.toIntExact(timer % 60); // get the rest
                String disMinu = (minutes < 10 ? "0" : "") + minutes; // get minutes and add "0" before if lower than 10
                String disSec = (seconds < 10 ? "0" : "") + seconds; // get seconds and add "0" before if lower than 10
                String formattedTime = disMinu + ":" + disSec; //get the whole time

                // Give the player a message that they need to wait.
                sender.sendMessage(Utils.formatMessage("Time remaining until you can perform another /pride: " + formattedTime, false));
                return true;
            }
        }

        String message = ChatColor.AQUA + player.getName() + " is showing their Pride: ";

        if (args.length == 0) {
            // Add default colors: The rainbow
            message += ChatColor.RED + "❤" + ChatColor.GOLD + "❤" + ChatColor.YELLOW + "❤" + ChatColor.GREEN + "❤" + ChatColor.BLUE + "❤" + ChatColor.DARK_PURPLE + "❤";
        } else {
            ConfigurationSection configSection = Utils.getPlugin().getConfig().getConfigurationSection("pride_options");
            if (!configSection.contains(args[0].toLowerCase())) message += ChatColor.RED + "❤" + ChatColor.GOLD + "❤" + ChatColor.YELLOW + "❤" + ChatColor.GREEN + "❤" + ChatColor.BLUE + "❤" + ChatColor.DARK_PURPLE + "❤";
            else {
                for (char c : configSection.getString(args[0].toLowerCase()).toCharArray()) {
                    message += ChatColor.translateAlternateColorCodes('&', "&" + c + "❤");
                }
            }
        }

        // Since we want individual players to be able to NOT see a hug, iterate over everyone online
        for (Player receiver : Utils.getPlugin().getServer().getOnlinePlayers()) {
            if (player.canSee(receiver)) {
                // Check to see if the receiver has toggled status to deny pride hearts.
                UUID UUID = receiver.getUniqueId();

                YamlConfiguration playersYml = YamlConfiguration.loadConfiguration(Utils.getPlayersFile());
                String currentToggle = playersYml.getString(UUID.toString());
                String currentTogglePride = playersYml.getString(UUID.toString() + ".pride");

                // If there is an entry and it is set to "off", return an apology and quit.
                if (!(
                    (currentToggle != null && !currentToggle.isEmpty() && currentToggle.equalsIgnoreCase("off")) ||
                    (currentTogglePride != null && !currentTogglePride.isEmpty() && currentTogglePride.equalsIgnoreCase("off"))
                )) {
                    receiver.sendMessage(message);
                }
            }
        }

        return true;
    }

}