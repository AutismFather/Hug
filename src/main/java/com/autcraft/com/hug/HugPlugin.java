package com.autcraft.com.hug;

import com.autcraft.com.hug.commands.HugCommand;
import com.autcraft.com.hug.commands.GroupHugCommand;
import com.autcraft.com.hug.commands.HugToggleCommand;
import com.autcraft.com.hug.commands.PrideCommand;

import com.autcraft.com.hug.tabcompleters.PrideTabCompleter;

import org.bukkit.plugin.java.JavaPlugin;

public final class HugPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Utils.log("Starting up");

        // Initialize the config
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        Utils.SetPlugin(this);

        getCommand("hug").setExecutor(new HugCommand());
        getCommand("grouphug").setExecutor(new GroupHugCommand());
        getCommand("hugtoggle").setExecutor(new HugToggleCommand());
        
        getCommand("pride").setExecutor(new PrideCommand());
        getCommand("pride").setTabCompleter(new PrideTabCompleter());
    }

    @Override
    public void onDisable() {
        Utils.log("Shutting down");
    }
    
}
