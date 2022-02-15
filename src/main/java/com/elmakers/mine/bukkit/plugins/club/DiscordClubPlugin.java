package com.elmakers.mine.bukkit.plugins.club;

import org.bukkit.plugin.java.JavaPlugin;

public class DiscordClubPlugin extends JavaPlugin {
    private ClubController controller;

    public void onEnable() {
        saveDefaultConfig();
        controller = new ClubController(this);

        // Register commands
        CommandProcessor processor = new CommandProcessor(this, controller);
        getCommand("eliza").setTabCompleter(processor);
        getCommand("eliza").setExecutor(processor);
    }

    public void onDisable() {
        controller.shutdown();
    }

    public ClubController getController() {
        return controller;
    }
}
