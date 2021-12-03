package com.elmakers.mine.bukkit.plugins.brawl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;

public class DiscordBrawlPlugin extends JavaPlugin {
    private YamlConfiguration messages;
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

    public void onEnable() {
        saveDefaultConfig();
        messages = new YamlConfiguration();
        try {
            InputStream resource = getResource("messages.yml");
            if (resource == null) {
                throw new Exception("messages.yml resource was null");
            }
            messages.load(new InputStreamReader(resource, StandardCharsets.UTF_8));
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Failed to load messages.yml resource", ex);
        }

        token = getConfig().getString("token", "");
        channel = getConfig().getString("channel", "");
        commandChannel = getConfig().getString("command_channel", "*");
        reactionChannel = getConfig().getString("reaction_channel", "*");
        mentionChannel = getConfig().getString("mention_channel", "*");
        ignoreChannel = getConfig().getString("ignore_channel", "");
        reactionEmote = getConfig().getString("reaction_emote", "");
        guildId = getConfig().getString("guild", "");
        joinRole = getConfig().getString("join_role", "");
        joinChannel = getConfig().getString("join_channel", "");
        mentionId = getConfig().getString("mention_id", "");
        command = getConfig().getString("command", "mhelp");
        debug = getConfig().getBoolean("debug", false);
        if (token == null || token.isEmpty()) {
            getLogger().warning("Please put your bot token in config.yml, otherwise this plugin can't work");
        } else {
            getServer().getScheduler().runTaskAsynchronously(this, new JDAConnector(this));
        }

        if (joinChannel != null && !joinChannel.isEmpty()) {
            getLogger().info("Sending join messages to " + joinChannel);
        }
    }

    public void onDisable() {
        if (jda != null) {
            jda.shutdownNow();
        }
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

    public String getMessage(String key) {
        return messages.getString(key);
    }
}
