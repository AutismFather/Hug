package com.autcraft.com.hug.commands;

import com.autcraft.com.hug.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class HugCommand implements CommandExecutor {

    /*
        Triggered by the /hug command
        - sender: person giving the hug
        - args: arguments, such as person they're hugging
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Be able to take this away from people if necessary.
        if (!sender.hasPermission("hug.hug")) {
            sender.sendMessage(Utils.formatMessage("Insufficient permissions for hugs.", false));
            return true;
        }

        // First check to see if args[0] is 'reload' because then it's a request to reload the config
        if (sender.hasPermission("hug.reload") && args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            Utils.reloadConfig();
            sender.sendMessage(Utils.formatMessage("Hug config reloaded.", true));
            return true;
        }

        // Fail if console. Consoles can not hug.
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(Utils.formatMessage("Sorry console, you can not hug. You have no arms.", false)); // Ugh, rude 🙄
            return true;
        }

        // If this is not a player running the command (who would it be then??), just exit out
        if (!(sender instanceof Player)) {
            return true;
        }

        // If they just did /hug by itself or did /hug help, show this information. Also turn on word wrapping. Don't just send the message 4 times silly :)
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(Utils.color(Utils.getPlugin().getConfig().getString("hug_help_string")));
            if (sender.hasPermission("hug.reload")) {
                sender.sendMessage(ChatColor.RED + "/hug reload " + ChatColor.GOLD + "- Reloads the config ");
            }
            sender.sendMessage(ChatColor.GOLD + "************************************************************");
            return true;
        }

        // Cast the sender object to player 'hugger' and if possible, cast the person being hugged to 'huggee'
        Player hugger = (Player) sender;
        Player huggee = Bukkit.getServer().getPlayer(args[0]);

        // If huggee is not a player, is it meant to be an easter egg?
        if (huggee == null || !hugger.canSee(huggee)) {
            // Check for an easter egg message. If there is one, return it. Otherwise, continue on.
            String easter_egg_message = easterEgg(args[0], hugger);
            if (easter_egg_message != null) {
                sender.sendMessage(Utils.formatMessage(easter_egg_message, true));
                return true;
            }

            // Exit either way since we can't progress if it's not a player that they're trying to hug
            sender.sendMessage(Utils.formatMessage(args[0] + " is not online.", false));
            return true;
        }

        if (!hugger.hasPermission("hug.cooldown_bypass")) {
            // If there is still a cooldown timer in place, no hugs!
            int cooldown_timer = Utils.getPlugin().getConfig().getInt("cooldown_timer");

            long timer = Utils.cooldown(hugger, cooldown_timer * 1000, "hug");
            if (timer > 0) {
                sender.sendMessage(Utils.formatMessage("Sorry, only one hug every " + cooldown_timer + " seconds!", false));
                return true;
            }
        }

        // Check to see if the huggee has toggled status to deny hugs.
        UUID UUID = hugger.getUniqueId();

        // Does the huggee want to be hugged? Check the players.yml
        YamlConfiguration playersYml = YamlConfiguration.loadConfiguration(Utils.getPlayersFile());
        String currentToggle = playersYml.getString(UUID.toString());
        String currentToggleHugs = playersYml.getString(UUID.toString() + ".hugs");

        // If there is an entry and it is set to "off", return an apology and quit.
        // DEPRECATED
        if (currentToggleHugs != null && currentToggleHugs.equalsIgnoreCase("off")) {
            sender.sendMessage(Utils.formatMessage(huggee.getName() + " is not accepting hugs at the moment. Sorry.", false));
            return true;
        }

        // Get the hugger and huggee's locations
        String playerWorld = hugger.getWorld().getName();
        String huggeeWorld = huggee.getWorld().getName();
        Location playerLocation = hugger.getLocation();
        if (!playerWorld.equals(huggeeWorld) || playerLocation.distance(huggee.getLocation()) > Utils.getPlugin().getConfig().getInt("maximum_hug_distance")) {
            sender.sendMessage(Utils.formatMessage(huggee.getName() + " is too far away to hug!", false));
            return true;
        }

        // Finally, send the hug messages
        huggee.sendMessage(Utils.formatMessage(Utils.getPlugin().getConfig().getString("hug_receive_string")
            .replaceAll("%hug_receiver%", huggee.getName())
            .replaceAll("%hug_sender%", hugger.getName()), true));

        hugger.sendMessage(Utils.formatMessage(Utils.getPlugin().getConfig().getString("hug_send_string")
            .replaceAll("%hug_receiver%", huggee.getName())
            .replaceAll("%hug_sender%", hugger.getName()), true));

        return true;
    }

    /*
    Check for an easter egg. If there is one, return it's text.
    */
    public String easterEgg(String input, Player sender) {
        // Get the easter eggs from the config file.
        ConfigurationSection configSection = Utils.getPlugin().getConfig().getConfigurationSection("easter_eggs");

        // If the entered text is not an easter egg, simply return null.
        if (!configSection.contains(input.toLowerCase())) return null;

        // Get each optional response, pick one at random
        List options = configSection.getList(input.toLowerCase());
        Random r = new Random();
        int key = r.nextInt(options.toArray().length - 0);

        return options.get(key).toString();
    }

}