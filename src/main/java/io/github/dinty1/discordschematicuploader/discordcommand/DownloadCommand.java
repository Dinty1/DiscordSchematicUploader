package io.github.dinty1.discordschematicuploader.discordcommand;

import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;
import io.github.dinty1.discordschematicuploader.util.MessageUtil;
import io.github.dinty1.discordschematicuploader.util.RoleUtil;

import java.awt.*;
import java.io.File;
import java.util.Objects;

public class DownloadCommand {

    private static final DiscordSchematicUploader plugin = DiscordSchematicUploader.getPlugin();

    // I hate myself for making this static....
    public static void execute(DiscordGuildMessageReceivedEvent event, File schematicFolder) {
        final Message message = event.getMessage();
        final String downloadCommand = Objects.requireNonNull(DiscordSchematicUploader.getPlugin().getConfig().getString("download-command"));

        if (!RoleUtil.hasAllowedRole(event.getMember(), DiscordSchematicUploader.getPlugin().getConfig().getStringList("download-command-allowed-roles"))) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "You do not have permission to execute this command.").build()).queue();
        }
        // Make sure there's a schem name specified
        else if (message.getContentRaw().trim().equals(downloadCommand)) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "You must specify the name of the schematic that you want to download.").build()).queue();
        } else {
            // Adding 1 to the length because the arg is specified after a space
            final String[] args = message.getContentRaw().substring(downloadCommand.length() + 1).split(" ");

            String schematicFileExtension = ".schem";

            // Make sure that it exists in one form or another
            if (!new File(schematicFolder, args[0] + ".schem").exists()) {
                if (!new File(schematicFolder, args[0] + ".schematic").exists()) {
                    message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "That schematic doesn't seem to exist.").build()).queue();
                    return;
                } else {
                    schematicFileExtension = ".schematic";
                }
            }

            String finalSchematicFileExtension = schematicFileExtension;
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.GRAY, message.getAuthor(), "Attempting to download schematic `" + args[0] + schematicFileExtension + "`...").build()).queue(sentMessage -> {
                final File schematicToDownload = new File(schematicFolder, args[0] + finalSchematicFileExtension);
                try {
                    if (plugin.getConfig().getBoolean("send-downloaded-schematic-privately")) {
                        event.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Here you go!").addFile(schematicToDownload).queue(msg -> {
                            sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.GREEN, message.getAuthor(), "Download successful! Check your direct messages.").build()).queue();
                        }, t -> notifyDirectMessageError(sentMessage, message)), t -> notifyDirectMessageError(sentMessage, message));
                    } else {
                        message.getChannel().sendMessage("Here you go!").addFile(schematicToDownload).queue(sentSchematicMessage ->
                                sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.GREEN, message.getAuthor(), "Download successful!").build()).queue());
                    }
                } catch (IllegalArgumentException e) {
                    sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "An error occurred when trying to download the schematic. The most likely cause is that it is too large to upload to Discord!").build()).queue();
                } catch (Exception e) {
                    plugin.getLogger().severe(e.getMessage());
                    e.printStackTrace();
                    sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "An unknown error occurred when trying to download the schematic. Please check the server console for more details.").build()).queue();
                }
            });

        }
    }

    private static void notifyDirectMessageError(Message sentMessage, Message originalMessage) {
        sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, originalMessage.getAuthor(), "I was unable to send you a direct message.").build()).queue();
    }
}
