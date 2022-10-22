package com.autcraft.com.hug.tabcompleters;

import com.autcraft.com.hug.Utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class PrideTabCompleter implements TabCompleter {

    public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        List<String> result = new ArrayList<String>();

        for (String option : Utils.getPlugin().getConfig().getConfigurationSection("pride_options").getKeys(false)) {
            if (option.contains(args[args.length - 1])) {
                result.add(option);
            }
        }

        return result;
    }

}