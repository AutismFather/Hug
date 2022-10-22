package com.autcraft.com.hug.commands;

import com.autcraft.com.hug.Utils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GroupHugCommand implements CommandExecutor {

    /*
        Triggered by the /grouphug command
        - zero arguments, simple sends a message to each player stating that they've been hugged.
     */
     @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Be able to take this away from people if necessary.
        if (sender.hasPermission("hug.grouphug") == false) {
            sender.sendMessage(Utils.formatMessage("Insufficient permissions for grouphugs.", false));
            return true;
        }

        // Get player
        Player player = (Player) sender;
        Integer count = 0;

        if (!player.hasPermission("hug.cooldown_bypass")) {
            // If there is still a cooldown timer in place, no hugs!
            int grouphug_timer = Utils.getPlugin().getConfig().getInt("grouphug_timer");

            long timer = Utils.cooldown(player, grouphug_timer * 1000, "grouphug");
            if (timer > 0) {
                int minutes = Math.toIntExact(timer / 60); // convert seconds (saved in "time") to minutes
                int seconds = Math.toIntExact(timer % 60); // get the rest
                String disMinu = (minutes < 10 ? "0" : "") + minutes; // get minutes and add "0" before if lower than 10
                String disSec = (seconds < 10 ? "0" : "") + seconds; // get seconds and add "0" before if lower than 10
                String formattedTime = disMinu + ":" + disSec; //get the whole time

                // Give the player a message that they need to wait.
                sender.sendMessage(Utils.formatMessage("Time remaining until you can perform another group hug: " + formattedTime, false));
                return true;
            }
        }

        // Since we want individual players to be able to NOT see a hug, iterate over everyone online
        for (Player huggee : Utils.getPlugin().getServer().getOnlinePlayers()) {
            // If the player is not visible (id, vanish), then don't include them
            if (player.canSee(huggee)) {
                // Check to see if the huggee has toggled status to deny hugs.
                UUID UUID = huggee.getUniqueId();

                // Does the huggee want to be hugged? Check the players.yml
                YamlConfiguration playersYml = YamlConfiguration.loadConfiguration(Utils.getPlayersFile());
                String currentToggle = playersYml.getString(UUID.toString());
                String currentToggleGrouphugs = playersYml.getString(UUID.toString() + ".grouphugs");

                // If there is an entry and it is set to "off", return an apology and quit.
                if (!(
                    (currentToggle != null && !currentToggle.isEmpty() && currentToggle.equalsIgnoreCase("off")) ||
                    (currentToggleGrouphugs != null && !currentToggleGrouphugs.isEmpty() && currentToggleGrouphugs.equalsIgnoreCase("off"))
                )) {
                    // If the player is still receiving hugs, give them the message
                    huggee.sendMessage(ChatColor.AQUA + sender.getName() + " " + Utils.getPlugin().getConfig().getString("grouphug_string"));
                    count++;
                }
            }
        }

        player.sendMessage(ChatColor.GREEN + "You gave " + count + " people a group hug!");

        return true;
    }

}