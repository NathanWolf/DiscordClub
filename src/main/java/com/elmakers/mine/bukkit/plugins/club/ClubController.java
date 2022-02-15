package com.elmakers.mine.bukkit.plugins.club;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;

import codeanticode.eliza.Eliza;
import codeanticode.eliza.FileLoader;

public class ClubController implements FileLoader {
    private final Plugin plugin;
    private final YamlConfiguration messages;
    private final Eliza eliza;
    private String token;
    private String channel;
    private String commandChannel;
    private String mentionChannel;
    private String mentionId;
    private String reactionChannel;
    private String ignoreChannel;
    private String reactionEmote;
    private String joinRole;
    private String joinChannel;
    private String guildId;
    private String command;
    private boolean debug;
    private JDA jda = null;

    public ClubController(Plugin plugin) {
        this.plugin = plugin;
        eliza = new Eliza(this);

        // Load messages
        messages = new YamlConfiguration();
        try {
            InputStream resource = plugin.getResource("messages.yml");
            if (resource == null) {
                throw new Exception("messages.yml resource was null");
            }
            messages.load(new InputStreamReader(resource, StandardCharsets.UTF_8));
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load messages.yml resource", ex);
        }

        // Load configuration
        token = plugin.getConfig().getString("token", "");
        channel = plugin.getConfig().getString("channel", "");
        commandChannel = plugin.getConfig().getString("command_channel", "*");
        reactionChannel = plugin.getConfig().getString("reaction_channel", "*");
        mentionChannel = plugin.getConfig().getString("mention_channel", "*");
        ignoreChannel = plugin.getConfig().getString("ignore_channel", "");
        reactionEmote = plugin.getConfig().getString("reaction_emote", "");
        guildId = plugin.getConfig().getString("guild", "");
        joinRole = plugin.getConfig().getString("join_role", "");
        joinChannel = plugin.getConfig().getString("join_channel", "");
        mentionId = plugin.getConfig().getString("mention_id", "");
        command = plugin.getConfig().getString("command", "mhelp");
        debug = plugin.getConfig().getBoolean("debug", false);
        if (token == null || token.isEmpty()) {
            getLogger().warning("Please put your bot token in config.yml, otherwise this plugin can't work");
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new JDAConnector(this));
        }

        if (joinChannel != null && !joinChannel.isEmpty()) {
            getLogger().info("Sending join messages to " + joinChannel);
        }
    }

    public String respond(String message) {
        return eliza.processInput(message);
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdownNow();
        }
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }

    public String getMessage(String key) {
        return messages.getString(key);
    }

    public String getToken() {
        return token;
    }

    public String getChannel() {
        return channel;
    }

    public String getWelcomeChannel() {
        return joinChannel != null && !joinChannel.isEmpty() ? joinChannel : channel;
    }

    public String getReactionChannel() {
        return reactionChannel;
    }

    public String getIgnoreChannel() {
        return ignoreChannel;
    }

    public String getReactionEmote() {
        return reactionEmote;
    }

    public String getCommandChannel() {
        return commandChannel;
    }

    public String getMentionChannel() {
        return mentionChannel;
    }

    public String getMentionId() {
        return mentionId;
    }

    public String getJoinRole() {
        return joinRole;
    }

    public String getCommand() {
        return command;
    }

    public String getGuild() {
        return guildId;
    }

    public boolean isDebug() {
        return debug;
    }

    protected void setJDA(JDA jda) {
        this.jda = jda;
        jda.getPresence().setActivity(Activity.playing(getMessage("discord.status")));
        getLogger().info("Connected to the Discord server!");
    }

    @Override
    public String[] readFile(String filename) {
        List<String> lines = new ArrayList<>();
        try {
            InputStream resource = plugin.getResource(filename);
            if (resource == null) {
                throw new Exception(filename + " resource was null");
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            }
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load messages.yml resource", ex);
        }
        return lines.toArray(new String[]{});
    }
}
