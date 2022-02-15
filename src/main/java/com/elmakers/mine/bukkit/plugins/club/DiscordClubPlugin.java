package com.elmakers.mine.bukkit.plugins.club;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordClubPlugin extends JavaPlugin {
    private ClubController controller;

    public void onEnable() {
        saveDefaultConfig();
        controller = new ClubController(this);

        // Register commands
        CommandProcessor processor = new CommandProcessor(this, controller);
        setupCommand("eliza", processor);
    }

    protected void setupCommand(String commandName, CommandProcessor processor) {
        PluginCommand command = getCommand(commandName);
        if (command != null) {
            command.setTabCompleter(processor);
            command.setExecutor(processor);
        }
    }

    public void onDisable() {
        controller.shutdown();
    }

    public ClubController getController() {
        return controller;
    }
}
