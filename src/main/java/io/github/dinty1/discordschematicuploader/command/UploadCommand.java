package io.github.dinty1.discordschematicuploader.command;

import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;
import io.github.dinty1.discordschematicuploader.util.MessageUtil;
import io.github.dinty1.discordschematicuploader.util.RoleUtil;

import java.awt.*;
import java.io.File;

public class UploadCommand {

    public static void execute(DiscordGuildMessageReceivedEvent event, File schematicFolder) {
        final Message message = event.getMessage();
        final Message.Attachment attachment = message.getAttachments().size() > 0 ? message.getAttachments().get(0) : null;

        if (!RoleUtil.hasAllowedRole(event.getMember(), DiscordSchematicUploader.getPlugin().getConfig().getStringList("upload-command-allowed-roles"))) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "You do not have permission to execute this command.").build()).queue();
        } else if (attachment == null) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "You need to attach a file to upload.").build()).queue();
        } else if (attachment.getFileExtension() == null || !(attachment.getFileExtension().equals("schem") || attachment.getFileExtension().equals("schematic"))) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "That's not a valid schematic file.").build()).queue();
        } else { // Seems legit
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.GRAY, message.getAuthor(), "Attempting to save schematic...").build()).queue(sentMessage -> {

                // Make sure the schematic doesn't already exist
                File downloadedSchematic = new File(schematicFolder, attachment.getFileName());
                if (downloadedSchematic.exists()) {
                    sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "The schematic `" + attachment.getFileName() + "` already exists.").build()).queue();
                    return;
                }
                try {
                    attachment.downloadToFile(downloadedSchematic);
                    sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.GREEN, message.getAuthor(), "Schematic successfully saved as `" + attachment.getFileName() + "`.").build()).queue();
                } catch (Exception e) {
                    DiscordSchematicUploader.getPlugin().getLogger().severe(e.getMessage());
                    e.printStackTrace();
                    sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "An error occurred when trying to save the schematic. Please check the server console for more details.").build()).queue();
                }

            });
        }
    }
}
