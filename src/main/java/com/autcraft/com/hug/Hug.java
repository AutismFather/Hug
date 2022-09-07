/*
 * This plugin was created by AutismFather for the Autcraft and Autcraft Obsidian servers.
 * No other servers are permitted to use this plugin in part or in it's entirety.
 */
package com.autcraft.com.hug;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author Stuart Duncan (AutismFather)
 */
public final class Hug extends JavaPlugin {

    private final File playersFile = new File(getDataFolder(), "players.yml");
    private static final HashMap<String, Long> hug_cds = new HashMap<String, Long>();
    private static final HashMap<String, Long> grouphug_cds = new HashMap<String, Long>();
    private static final HashMap<String, Long> pride_cds = new HashMap<String, Long>();
    private String prefix;
    private int cooldown_timer;
    private int grouphug_timer;
    private int pride_timer;
    private int hug_distance;
    private String hug_receive_string;
    private String hug_send_string;
    private String grouphug_string;
    private java.util.UUID UUID;
    private final boolean red = false;


    @Override
    public void onEnable() {
        /* initialize the config */
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        // Set Defaults (if not already in the config
        if (!getConfig().contains("hug_receive_string", true)) {
            getConfig().addDefault("hug_receive_string", "%hug_sender% gives you a big hug! {^-^}");
            Bukkit.getConsoleSender().sendMessage("Added default string to config.yml: hug_receive_string");
            saveConfig();
        }
        if (!getConfig().contains("hug_send_string", false)) {
            getConfig().addDefault("hug_send_string", "You gave a giant hug to %hug_receiver%");
            Bukkit.getConsoleSender().sendMessage("Added default string to config.yml: hug_send_string");
            saveConfig();
        }
        if (!getConfig().contains("hug_help_string", false)) {
            getConfig().addDefault("hug_help_string", "&6************************************************************\n&c/hug <player> &6- Give the <player> a hug\n&c/grouphug - Give the server a giant group hug\n&c/pride&6 - Shows your Pride in the chat for all to see\n&c/hugtoggle <hugs/grouphugs/pride> <on/off> &6- Turns off the messages in your chat");
            Bukkit.getConsoleSender().sendMessage("Added help message to config.");
            saveConfig();
        }

        // If not already created, make our players.yml file
        if (!playersFile.exists()) {
            try {
                playersFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create players.yml");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Get defaults
        prefix = getConfig().getString("prefix");
        cooldown_timer = getConfig().getInt("cooldown_timer");
        grouphug_timer = getConfig().getInt("grouphug_timer");
        pride_timer = getConfig().getInt("pride_timer");
        hug_distance = getConfig().getInt("maximum_hug_distance");
        hug_receive_string = getConfig().getString("hug_receive_string");
        hug_send_string = getConfig().getString("hug_send_string");
        grouphug_string = getConfig().getString("grouphug_string");
        hug_help_string = getConfig().getString("hug_help_string");

        if (command.getName().equalsIgnoreCase("hug")) {
            return hug(sender, args);
        } else if (command.getName().equalsIgnoreCase("hugtoggle")) {
            return hugtoggle(sender, args);
        } else if (command.getName().equalsIgnoreCase("grouphug")) {
            return grouphug(sender);
        } else if (command.getName().equalsIgnoreCase("pride")) {
            return pride(sender);
        }
        return false;
    }

    /*
        Triggered by the /hug command
        - sender: person giving the hug
        - args: arguments, such as person they're hugging
     */
    public boolean hug(CommandSender sender, String[] args) {
        // Be able to take this away from people if necessary.
        if (sender.hasPermission("hug.hug") == false) {
            sender.sendMessage(msg("Insufficient permissions for hugs", true));
            return true;
        }

        // First check to see if args[0] is 'reload' because then it's a request to reloag the config
        if (sender.hasPermission("hug.reload") && args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendMessage(msg("Hug Config Reloaded"));
            return true;
        }

        // Fail if console. Consoles can not hug.
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(msg("Sorry console, you can not hug. You have no arms.", true));
            return true;
        }

        // If this is not a player running the command (who would it be then??), just exit out
        if (!(sender instanceof Player)) {
            return true;
        }

        // If they just did /hug by itself or did /hug help, show this information. Also turn on word wrapping. Don't just send the message 4 times silly :)
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(colorUtils.color(hug_help_string));
            if (sender.hasPermission("hug.reload")) {
                sender.sendMessage(ChatColor.RED + "/hug reload " + ChatColor.GOLD + "- Reloads the config ");
            }
            sender.sendMessage(ChatColor.GOLD + "************************************************************");
            return true;
        }

        // Cast the sender object to player 'hugger' and if possible, cast the person being hugged to 'huggee'
        Player hugger = (Player) sender;
        Player huggee = getPlayer(args[0]);

        // If huggee is not a player, is it meant to be an easter egg?
        if (huggee == null || !hugger.canSee(huggee)) {
            // Check for an easter egg message. If there is one, return it. Otherwise, continue on.
            String easter_egg_message = easterEgg(args[0], hugger);
            if (easter_egg_message != null) {
                sender.sendMessage(msg(easter_egg_message));
                return true;
            }

            // Exit either way since we can't progress if it's not a player that they're trying to hug
            sender.sendMessage(msg(args[0] + " is not online", true));
            return true;
        }

        // Bypass the cooldown?
        if (hugger.hasPermission("hug.cooldown_bypass")) {
        } else {
            // If there is still a cooldown timer in place, no hugs!
            long timer = cooldown(hugger, cooldown_timer * 1000, "hug");
            if (timer > 0) {
                sender.sendMessage(msg("Sorry, only one hug every " + cooldown_timer + " seconds!", true));
                return true;
            }
        }

        // Check to see if the huggee has toggled status to deny hugs.
        UUID = hugger.getUniqueId();

        // Does the huggee want to be hugged? Check the players.yml
        YamlConfiguration playersYml = YamlConfiguration.loadConfiguration(playersFile);
        String currentToggle = playersYml.getString(UUID.toString());
        String currentToggleHugs = playersYml.getString(UUID.toString() + ".hugs");

        // If there is an entry and it is set to "off", return an apology and quit.
        // DEPRECATED
        if (currentToggle != null && !currentToggle.isEmpty() && currentToggle.equalsIgnoreCase("off")) {
            sender.sendMessage(msg(huggee.getName() + " is not accepting hugs at the moment. Sorry.", true));
            return true;
        } else if (currentToggleHugs != null && currentToggleHugs.equalsIgnoreCase("off")) {
            sender.sendMessage(msg(huggee.getName() + " is not accepting hugs at the moment. Sorry.", true));
            return true;
        }

        // Get the hugger and huggee's locations
        String playerWorld = hugger.getWorld().getName();
        String huggeeWorld = huggee.getWorld().getName();
        Location playerLocation = hugger.getLocation();
        if (playerWorld.equals(huggeeWorld) == false || playerLocation.distance(huggee.getLocation()) > hug_distance) {
            sender.sendMessage(msg(huggee.getName() + " is too far away to hug!", true));
            return true;
        }

        // Replace vars in stringers
        hug_receive_string = hug_receive_string.replaceAll("%hug_receiver%", huggee.getName());
        hug_receive_string = hug_receive_string.replaceAll("%hug_sender%", hugger.getName());
        hug_send_string = hug_send_string.replaceAll("%hug_receiver%", huggee.getName());
        hug_send_string = hug_send_string.replaceAll("%hug_sender%", hugger.getName());

        // Finally, send the hug messages
        huggee.sendMessage(msg(hug_receive_string));
        hugger.sendMessage(msg(hug_send_string));
        return true;
    }


    boolean pride(CommandSender sender) {

        // Permission restrictions in case abused
        if (sender.hasPermission("hug.pride") == false) {
            sender.sendMessage(msg("Insufficient permissions for /pride command", true));
            return true;
        }

        // The person sending the command
        Player player = (Player) sender;

        // Check to see if they have a cooldown or are able to bypass the cooldown.
        if (player.hasPermission("hug.cooldown_bypass")) {
            //System.out.print("bypass?");
        } else {
            // If there is still a cooldown timer in place, no hugs!
            long timer = cooldown(player, pride_timer * 1000, "pride");
            if (timer > 0) {
                int minutes = Math.toIntExact(timer / 60); // convert seconds (saved in "time") to minutes
                int seconds = Math.toIntExact(timer % 60); // get the rest
                String disMinu = (minutes < 10 ? "0" : "") + minutes; // get minutes and add "0" before if lower than 10
                String disSec = (seconds < 10 ? "0" : "") + seconds; // get seconds and add "0" before if lower than 10
                String formattedTime = disMinu + ":" + disSec; //get the whole time

                // Give the player a message that they need to wait.
                sender.sendMessage(msg("Time remaining until you can perform another /pride: " + formattedTime, true));
                return true;
            }
        }

        // Since we want individual players to be able to NOT see a hug, iterate over everyone online
        for (Player huggee : getServer().getOnlinePlayers()) {
            // If the player is not visible (id, vanish), then don't include them
            if (player.canSee(huggee) == false) {
            } else {
                // Check to see if the huggee has toggled status to deny hugs.
                UUID = huggee.getUniqueId();

                // Does the huggee want to be hugged? Check the players.yml
                YamlConfiguration playersYml = YamlConfiguration.loadConfiguration(playersFile);
                String currentToggle = playersYml.getString(UUID.toString());
                String currentToggleGrouphugs = playersYml.getString(UUID.toString() + ".pride");

                // If there is an entry and it is set to "off", return an apology and quit.
                if (currentToggle != null && !currentToggle.isEmpty() && currentToggle.equalsIgnoreCase("off")) {
                } else if (currentToggleGrouphugs != null && !currentToggleGrouphugs.isEmpty() && currentToggleGrouphugs.equalsIgnoreCase("off")) {
                } else {
                    // Broadcast to the server at once... commented out, not using this anymore.
                    // getServer().broadcastMessage(ChatColor.AQUA + sender.getName() + " " + grouphug_string);

                    // If the player is still receiving hugs, give them the message
                    huggee.sendMessage(ChatColor.AQUA + player.getName() + " is showing their Pride: " + ChatColor.RED + "❤" + ChatColor.GOLD + "❤" + ChatColor.YELLOW + "❤" + ChatColor.GREEN + "❤" + ChatColor.BLUE + "❤" + ChatColor.DARK_PURPLE + "❤");
                }
            }
        }

        return true;
    }


    /*
    Triggered by the /grouphug command
    - zero arguments, simple sends a message to each player stating that they've been hugged.
     */
    boolean grouphug(CommandSender sender) {

        // Be able to take this away from people if necessary.
        if (sender.hasPermission("hug.grouphug") == false) {
            sender.sendMessage(msg("Insufficient permissions for grouphugs", true));
            return true;
        }

        // Get player
        Player player = (Player) sender;
        Integer count = 0;

        // Check to see if they have a cooldown or are able to bypass the cooldown.
        if (player.hasPermission("hug.cooldown_bypass")) {
            //System.out.print("bypass?");
        } else {
            // If there is still a cooldown timer in place, no hugs!
            long timer = cooldown(player, grouphug_timer * 1000, "grouphug");
            if (timer > 0) {
                int minutes = Math.toIntExact(timer / 60); // convert seconds (saved in "time") to minutes
                int seconds = Math.toIntExact(timer % 60); // get the rest
                String disMinu = (minutes < 10 ? "0" : "") + minutes; // get minutes and add "0" before if lower than 10
                String disSec = (seconds < 10 ? "0" : "") + seconds; // get seconds and add "0" before if lower than 10
                String formattedTime = disMinu + ":" + disSec; //get the whole time

                // Give the player a message that they need to wait.
                sender.sendMessage(msg("Time remaining until you can perform another group hug: " + formattedTime, true));
                return true;
            }
        }

        // Since we want individual players to be able to NOT see a hug, iterate over everyone online
        for (Player huggee : getServer().getOnlinePlayers()) {
            // If the player is not visible (id, vanish), then don't include them
            if (player.canSee(huggee) == false) {
            } else {
                // Check to see if the huggee has toggled status to deny hugs.
                UUID = huggee.getUniqueId();

                // Does the huggee want to be hugged? Check the players.yml
                YamlConfiguration playersYml = YamlConfiguration.loadConfiguration(playersFile);
                String currentToggle = playersYml.getString(UUID.toString());
                String currentToggleGrouphugs = playersYml.getString(UUID.toString() + ".grouphugs");

                // If there is an entry and it is set to "off", return an apology and quit.
                if (currentToggle != null && !currentToggle.isEmpty() && currentToggle.equalsIgnoreCase("off")) {
                } else if (currentToggleGrouphugs != null && !currentToggleGrouphugs.isEmpty() && currentToggleGrouphugs.equalsIgnoreCase("off")) {
                } else {
                    // Broadcast to the server at once... commented out, not using this anymore.
                    // getServer().broadcastMessage(ChatColor.AQUA + sender.getName() + " " + grouphug_string);

                    // If the player is still receiving hugs, give them the message
                    huggee.sendMessage(ChatColor.AQUA + sender.getName() + " " + grouphug_string);
                    count = count + 1;
                }
            }
        }

        player.sendMessage(ChatColor.GREEN + "You gave " + count + " people a group hug!");

        return true;
    }

    /*
        Turns on and off your acceptance of receiving hugs.
        on = you are willing to receive a hug
        off = you do not wish to receive hugs
     */
    boolean hugtoggle(CommandSender sender, String[] args) {
        // Be able to take this away from people if necessary.
        if (sender.hasPermission("hug.hugtoggle") == false) {
            sender.sendMessage(msg("Insufficient permissions for hugtoggle", true));
            return true;
        }

        // Open the file
        YamlConfiguration playersYml = YamlConfiguration.loadConfiguration(playersFile);

        // Get player's UUID
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        // Defaults
        String toggle = "on";
        String toggle_hugs = "on";
        String toggle_grouphugs = "on";
        String toggle_pride = "on";

        // If there's no arguments passed, OR... the argument passed is not "hugs" and also not "grouphugs", output instructions
        if (args.length == 0 || (args[0].equalsIgnoreCase("hugs") == false && args[0].equalsIgnoreCase("grouphugs") == false && args[0].equalsIgnoreCase("pride") == false)) {
            player.sendMessage(ChatColor.AQUA + "Hugtoggle Usage:");
            player.sendMessage(ChatColor.AQUA + "/hugtoggle hugs on/off");
            player.sendMessage(ChatColor.AQUA + "/hugtoggle grouphugs on/off");
            player.sendMessage(ChatColor.AQUA + "/hugtoggle pride on/off");
            return true;
        }

        // Hugs?
        if (args[0].equalsIgnoreCase("hugs")) {
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
        }

        // Group Hugs?
        if (args[0].equalsIgnoreCase("grouphugs")) {
            // Set hugs to on
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
        }

        // Pride?
        if (args[0].equalsIgnoreCase("pride")) {
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
        }

        /*
        // default the toggle to on
        String toggle = "on";

        if (args.length > 0 && args[0].equalsIgnoreCase("on")) {
            toggle = "on";
        } else if (args.length > 0 && args[0].equalsIgnoreCase("off")) {
            toggle = "off";
        }
        // If nothing was entered OR something other than on/off, then get what it's currently set to and set it to the opposite.
        else {
            // Get the user's current toggle setting
            String currentToggle = playersYml.getString(uuid.toString());
            // If there was nothing in the file, or if there is but it is set to on, set it to off to deny hugs
            if (currentToggle == null || currentToggle.isEmpty() || currentToggle.equalsIgnoreCase("on")) {
                toggle = "off";
            } else {
                toggle = "on";
            }
        }

        // Save the new toggled setting
        playersYml.set(uuid.toString(), toggle);

         */

        // save the file
        try {
            playersYml.save(playersFile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe("Could not save players.yml");
        }

        // Now for the final message, this is mostly just to avoid showing "null" as a result if it has not yet been set
        toggle_hugs = playersYml.getString(uuid + ".hugs");
        if (toggle_hugs == null) {
            toggle_hugs = "on";
        }
        toggle_grouphugs = playersYml.getString(uuid + ".grouphugs");
        if (toggle_grouphugs == null) {
            toggle_grouphugs = "on";
        }
        toggle_pride = playersYml.getString(uuid + ".pride");
        if (toggle_pride == null) {
            toggle_pride = "on";
        }

        // Return a verification message to the player
        sender.sendMessage(ChatColor.GREEN + "Auto accept hugs? " + toggle_hugs);
        sender.sendMessage(ChatColor.GREEN + "Auto accept group hugs? " + toggle_grouphugs);
        sender.sendMessage(ChatColor.GREEN + "Auto accept pride hearts? " + toggle_pride);
        return true;
    }

    /*
    Just a handy place to pass all strings so that we can colour them as necessary
    */
    public String msg(String msg, boolean red) {
        // If red is true, return the message in red.
        if (red) {
            return ChatColor.RED + prefix + msg;
        }

        return ChatColor.GREEN + prefix + msg;
    }

    /*
    Overload, set red to false as default
    */
    public String msg(String msg) {
        return msg(msg, false);
    }

    /*
    Returns an instance of player, if that player exists and is online.
    Otherwise it returns null.
    */
    public Player getPlayer(String name) {
        Player player = Bukkit.getServer().getPlayer(name);
        return player;
    }

    /*
    Returns int timer - 0 if no time or time has run out or value remaining still in timer
    */
    public long cooldown(Player player, int seconds, String type) {
        // Amount of time remaining... 0 if none
        long timer = 0;
        // Set this to var so it can not change while the code is running (just in case)
        long currentTime = System.currentTimeMillis();

        // Check for hug timer vs group hug timer since they're different
        if (type.equalsIgnoreCase("hug")) {
            if (!hug_cds.containsKey(player.getName())) {
                hug_cds.put(player.getName(), currentTime);
            } else if (currentTime - seconds >= hug_cds.get(player.getName())) {
                hug_cds.put(player.getName(), currentTime);
            } else {
                timer = Math.toIntExact(seconds - (currentTime - hug_cds.get(player.getName()))) / 1000;
            }
        } else if (type.equalsIgnoreCase("grouphug")) {
            // Either add a new timer to the index, or check the time left on the timer
            if (!grouphug_cds.containsKey(player.getName())) {
                grouphug_cds.put(player.getName(), currentTime);
            } else if (currentTime == grouphug_cds.get(player.getName()) || currentTime - seconds >= grouphug_cds.get(player.getName())) {
                grouphug_cds.put(player.getName(), currentTime);
            } else {
                timer = Math.toIntExact(seconds - (currentTime - grouphug_cds.get(player.getName()))) / 1000;
            }
        } else if (type.equalsIgnoreCase("pride")) {
            // Either add a new timer to the index, or check the time left on the timer
            if (!pride_cds.containsKey(player.getName())) {
                pride_cds.put(player.getName(), currentTime);
            } else if (currentTime == pride_cds.get(player.getName()) || currentTime - seconds >= pride_cds.get(player.getName())) {
                pride_cds.put(player.getName(), currentTime);
            } else {
                timer = Math.toIntExact(seconds - (currentTime - pride_cds.get(player.getName()))) / 1000;
            }
        }

        return timer;
    }

    /*
    Check for an easter egg. If there is one, return it's text.
    */
    public String easterEgg(String input, Player sender) {
        // Get the easter eggs from the config file.
        ConfigurationSection configSection = getConfig().getConfigurationSection("easter_eggs");

        // If the entered text is not an easter egg, simply return false.
        if (configSection.contains(input.toLowerCase()) == false) {
            return null;
        }

        // Get each optional response, pick one at random
        List options = configSection.getList(input.toLowerCase());
        Random r = new Random();
        int key = r.nextInt(options.toArray().length - 0);

        return options.get(key).toString();
    }
}
