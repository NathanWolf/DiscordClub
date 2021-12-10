package com.elmakers.mine.bukkit.plugins.brawl;

import org.bukkit.plugin.java.JavaPlugin;

public class DiscordBrawlPlugin extends JavaPlugin {
    private BrawlController controller;

    public void onEnable() {
        saveDefaultConfig();
        controller = new BrawlController(this);

        // Register commands
        CommandProcessor processor = new CommandProcessor(this, controller);
        getCommand("eliza").setTabCompleter(processor);
        getCommand("eliza").setExecutor(processor);
    }

    public void onDisable() {
        controller.shutdown();
    }

    public BrawlController getController() {
        return controller;
    }
}
