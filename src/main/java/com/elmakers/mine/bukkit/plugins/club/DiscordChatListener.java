package com.elmakers.mine.bukkit.plugins.club;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

public class DiscordChatListener extends ListenerAdapter {
    private final ClubController controller;

    public DiscordChatListener(ClubController controller) {
        this.controller = controller;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        Role role = getJoinRole(guild);
        if (role == null) {
            return;
        }

        Member member = event.getMember();
        guild.addRoleToMember(member, role).queue(success -> controller.getLogger().info("Assigned join role to " + member.getEffectiveName()), throwable -> controller.getLogger().log(Level.SEVERE, "Failed to assign role to " + member.getEffectiveName(), throwable));

        String channelName = controller.getWelcomeChannel();
        List<TextChannel> helpChannels = guild.getTextChannelsByName(channelName, true);
        if (!helpChannels.isEmpty()) {
            MessageChannel helpChannel = helpChannels.get(0);
            String welcomeMessage = controller.getMessage("discord.welcome");
            welcomeMessage = welcomeMessage.replace("$member", member.getAsMention());
            MessageAction welcomeAction = helpChannel.sendMessage(welcomeMessage);
            welcomeAction.setActionRow(getFirstVerifyButton(member));
            welcomeAction.queue(success -> {}, throwable -> controller.getLogger().log(Level.SEVERE, "Failed to send welcome message to " + member.getEffectiveName(), throwable));
        }
    }

    protected Button getFirstVerifyButton(Member member) {
        return Button.success("verify:" + member.getId(), controller.getMessage("discord.verify_first_button"));
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
        String message = controller.respond(originalMessage.getContentStripped());
        MessageAction action = originalMessage.reply(message);
        action.queue(sentMessage -> sentMessage.suppressEmbeds(true).queue(), throwable -> controller.getLogger().log(Level.SEVERE, "Failed to send message in channel " + originalMessage.getChannel(), throwable));
    }

    private Role getJoinRole(Guild guild) {
        String joinRole = controller.getJoinRole();
        if (joinRole.isEmpty()) return null;
        Role role = guild.getRoleById(joinRole);
        if (role == null) {
            controller.getLogger().warning("Invalid join role id: " + joinRole);
        }
        return role;
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        MessageChannel channel = event.getChannel();
        if (channel.getName().equals(controller.getIgnoreChannel())) return;

        Button button = event.getButton();
        if (button == null) return;
        String id = button.getId();
        if (id == null) return;
        if (id.startsWith("verify:")) {
            final String memberId = id.substring(7);
            Member member = event.getMember();
            if (member == null) return;

            if (!member.getId().equals(memberId)) {
                ReplyAction action = event.reply(controller.getMessage("discord.verified_invalid"));
                action.setEphemeral(true);
                action.queue(sentMessage -> {}, throwable -> controller.getLogger().log(Level.SEVERE, "Failed to send message in response to button click " + id, throwable));
                return;
            }

            ReplyAction action = event.reply(controller.getMessage("discord.verified"));
            action.setEphemeral(true);
            action.queue(sentMessage -> {}, throwable -> controller.getLogger().log(Level.SEVERE, "Failed to send message in response to button click " + id, throwable));

            // Remove the join button when not on an ephemeral messages
            Message clickedMessage = event.getMessage();
            if (!clickedMessage.getFlags().contains(Message.MessageFlag.EPHEMERAL)) {
                List<ActionRow> actionRows = clickedMessage.getActionRows();
                if (!actionRows.isEmpty()) {
                    List<Component> keepButtons = new ArrayList<>();
                    for (ActionRow row : actionRows) {
                        for (Component component : row.getComponents()) {
                            if (!component.equals(button)) {
                                keepButtons.add(component);
                            }
                        }
                    }
                    MessageAction editMessage;
                    if (keepButtons.isEmpty()) {
                        editMessage = clickedMessage.editMessageComponents();
                    } else {
                        editMessage = clickedMessage.editMessageComponents(ActionRow.of(keepButtons));
                    }
                    editMessage.queue(sentMessage -> {}, throwable -> controller.getLogger().log(Level.SEVERE, "Failed to remove join button from message " + id, throwable));
                }
            }
        }
    }
}
