package com.elmakers.mine.bukkit.plugins.club;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;

public class ClubController {
    private final Plugin plugin;
    private final YamlConfiguration messages;
    private final OpenAiService ai;
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

        // Initialize OpenAI
        String apiKey = plugin.getConfig().getString("api_key", "");
        if (apiKey.isEmpty()) {
            throw new IllegalStateException("No api key specified");
        }
        ai = new OpenAiService(apiKey);

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

    public String respond(String prompt) {
        CompletionRequest completionRequest = CompletionRequest.builder()
                .temperature(0.9)
                .prompt(prompt)
                .echo(false)
                .maxTokens(150)
                .topP(1.0)
                .frequencyPenalty(0.0)
                .presencePenalty(0.6)
                .build();
        List<CompletionChoice> choices = ai.createCompletion("text-davinci-002", completionRequest).getChoices();
        for (CompletionChoice choice : choices) {
            if (choice.getFinish_reason().equals("stop")) {
                return choice.getText().trim();
            }
        }
        return "I don't know what to say.";
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
}
