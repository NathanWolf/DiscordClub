package com.elmakers.mine.bukkit.plugins.club;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandProcessor implements TabExecutor {
    private final Plugin plugin;
    private final ClubController controller;

    public CommandProcessor(Plugin plugin, ClubController controller) {
        this.plugin = plugin;
        this.controller = controller;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        try {
            onEvaluate(commandSender, args);
        } catch (Exception ex) {
            commandSender.sendMessage(ChatColor.RED + "Something went wrong!");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }

    private void onEvaluate(CommandSender sender, String[] args) {
        sender.sendMessage(controller.respond(StringUtils.join(args, " ")));
    }
}
