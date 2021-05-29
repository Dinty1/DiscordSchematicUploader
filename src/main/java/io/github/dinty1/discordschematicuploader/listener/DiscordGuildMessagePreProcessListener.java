package io.github.dinty1.discordschematicuploader.listener;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;
import io.github.dinty1.discordschematicuploader.util.MessageUtil;
import org.bukkit.Bukkit;

import java.awt.*;
import java.io.File;
import java.util.Objects;

public class DiscordGuildMessagePreProcessListener {
    private DiscordSchematicUploader plugin;

    public DiscordGuildMessagePreProcessListener(DiscordSchematicUploader plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onDiscordMessage(DiscordGuildMessagePreProcessEvent event) {
        final String uploadCommand = Objects.requireNonNull(plugin.getConfig().getString("upload-command"));
        final String downloadCommand = Objects.requireNonNull(plugin.getConfig().getString("download-command"));

        final File worldeditDataFolder = Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("WorldEdit")).getDataFolder();
        final File schematicFolder = new File(worldeditDataFolder, "schematics");

        Message message = event.getMessage();
        Message.Attachment attachment = message.getAttachments().size() > 0 ? message.getAttachments().get(0) : null;

        // If it's the upload command
        if (message.getContentRaw().startsWith(uploadCommand)) {
            event.setCancelled(true);
            if (attachment == null) {
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
                        plugin.getLogger().severe(e.getMessage());
                        e.printStackTrace();
                        sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "An error occurred when trying to save the schematic. Please check the server console for more details.").build()).queue();
                    }

                });
            }
        } else if (message.getContentRaw().startsWith(downloadCommand)) {
            event.setCancelled(true);

            // Make sure there's a schem name specified
            if (message.getContentRaw().trim().equals(downloadCommand)) {
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
                        message.getChannel().sendMessage("Here you go!").addFile(schematicToDownload).queue(sentSchematicMessage -> {
                            sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.GREEN, message.getAuthor(), "Download successful!").build()).queue();
                        });
                    } catch (IllegalArgumentException e) {
                        sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "An error occurred when trying to download the schematic. The most likely cause is that it is too large to upload to Discord!").build()).queue();
                    }
                    catch (Exception e) {
                        plugin.getLogger().severe(e.getMessage());
                        e.printStackTrace();
                        sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "An unknown error occurred when trying to download the schematic. Please check the server console for more details.").build()).queue();
                    }
                });

            }
        }
    }
}
