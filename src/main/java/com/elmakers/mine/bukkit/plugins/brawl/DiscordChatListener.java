package com.elmakers.mine.bukkit.plugins.brawl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class DiscordChatListener extends ListenerAdapter {
    private final DiscordBrawlPlugin controller;

    public DiscordChatListener(DiscordBrawlPlugin controller) {
        this.controller = controller;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();

        Member member = event.getMember();
        String channelName = controller.getWelcomeChannel();
        List<TextChannel> helpChannels = guild.getTextChannelsByName(channelName, true);
        if (!helpChannels.isEmpty()) {
            MessageChannel helpChannel = helpChannels.get(0);
            String welcomeMessage = controller.getMessage("discord.welcome");
            welcomeMessage = welcomeMessage.replace("$member", member.getAsMention());
            MessageAction welcomeAction = helpChannel.sendMessage(welcomeMessage);
            welcomeAction.queue(success -> {}, throwable -> controller.getLogger().log(Level.SEVERE, "Failed to send welcome message to " + member.getEffectiveName(), throwable));
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        if (author.isBot()) return;
        MessageChannel channel = event.getChannel();
        if (channel.getName().equals(controller.getIgnoreChannel())) return;

        Message message = event.getMessage();
        List<Member> members = message.getMentionedMembers();

        boolean mentioned = false;
        String mentionChannel = controller.getMentionChannel();
        String mentionId = controller.getMentionId();
        List<Member> mention = new ArrayList<>();

        for (Member member : members) {
            if (member.getId().equals(mentionId)) {
                mentioned = true;
            } else {
                mention.add(member);
            }
        }

        if (!mentionChannel.equals("*") && !mentionChannel.equals(channel.getName())) {
            mentioned = false;
        }

        // Only listen to a specific channel, unless mentioned
        if (!mentioned && !channel.getName().equals(controller.getChannel())) return;

        // Ignore replies, unless mentioning
        if (!mentioned && message.getMessageReference() != null) return;
        respondToMessage(message, mention);
    }

    protected void respondToMessage(Message originalMessage, List<Member> mentions) {
        String message = "I'm alive!";
        MessageAction action = originalMessage.reply(message);
        action.queue(sentMessage -> sentMessage.suppressEmbeds(true).queue(), throwable -> controller.getLogger().log(Level.SEVERE, "Failed to send message in channel " + originalMessage.getChannel(), throwable));
    }
}
