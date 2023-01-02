/*-
 * LICENSE
 * DiscordSchematicUploader
 * -------------
 * Copyright (C) 2021 Dinty1
 * -------------
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * END
 */

package io.github.dinty1.discordschematicuploader.discordcommand;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;
import io.github.dinty1.discordschematicuploader.util.*;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UploadCommand {

    public static void execute(DiscordGuildMessageReceivedEvent event, File schematicFolder, boolean fastAsyncWorldEditEnabled) {
        final Message message = event.getMessage();
        final Message.Attachment attachment = message.getAttachments().size() > 0 ? message.getAttachments().get(0) : null;
        final DiscordSchematicUploader plugin = DiscordSchematicUploader.getPlugin();
        final List<String> flags = MessageUtil.getFlags(event.getMessage(), Objects.requireNonNull(plugin.getConfig().getString("upload-command")));


        if (!RoleUtil.hasAllowedRole(event.getMember(), "upload-command-allowed-roles")) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_NO_PERMISSION.toString()).build()).queue();
        } else if (attachment == null) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_NO_ATTACHMENT.toString()).build()).queue();
        } else if (attachment.getFileExtension() == null || !(attachment.getFileExtension().equals("schem") || attachment.getFileExtension().equals("schematic"))) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_INVALID_SCHEMATIC_FILE.toString()).build()).queue();
        } else { // Seems legit
            boolean uploadingToGlobal = true;
            if (fastAsyncWorldEditEnabled) {
                if ((!flags.contains("g") && !plugin.getConfig().getBoolean("upload-command-default-to-global")) || flags.contains("p")) {
                    UUID uuid = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(event.getAuthor().getId());
                    if (uuid == null) {
                        message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.NOT_LINKED.toString()).build()).queue();
                        return;
                    }

                    schematicFolder = new File(schematicFolder, uuid.toString());
                    try {
                        if (!schematicFolder.exists()) schematicFolder.mkdir();
                    } catch (Exception ignored) { } // There'll be an exception dealt with later on :shrug:
                    uploadingToGlobal = false;
                } else { // Going to global so we need to check that user has permission
                    if (!RoleUtil.hasAllowedRole(event.getMember(), "global-upload-allowed-roles")) {
                        message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.GLOBAL_UPLOAD_NO_PERMISSION.toString()).build()).queue();
                        if (plugin.getConfig().getBoolean("upload-command-delete-original-message")) message.delete().queue();
                        return;
                    }
                }
            }

            File finalSchematicFolder = schematicFolder; // Keep the lambda happy
            boolean finalUploadingToGlobal = uploadingToGlobal;
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.GRAY, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_ATTEMPTING_SCHEMATIC_SAVE.toString()).build()).queue(sentMessage -> {

                String fileName = attachment.getFileName();
                if (finalUploadingToGlobal || plugin.getConfig().getBoolean("use-name-format-for-personal-uploads")) fileName = ConfigUtil.formatSchematicName(attachment, event.getMember());

                // Make sure the schematic doesn't already exist
                final File downloadedSchematic = new File(finalSchematicFolder, fileName);
                final boolean allowedToOverwrite = RoleUtil.hasAllowedRole(event.getMember(), "upload-command-allowed-to-overwrite");
                if (downloadedSchematic.exists()) {
                    if (flags.contains("o") && allowedToOverwrite) {
                        downloadedSchematic.delete();
                    } else {
                        final String overwriteMessage = allowedToOverwrite ? " " + ConfigUtil.Message.UPLOAD_COMMAND_CAN_OVERWRITE : "";
                        sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_SCHEMATIC_ALREADY_EXISTS.toString(fileName) + overwriteMessage).build()).queue();
                        if (plugin.getConfig().getBoolean("upload-command-delete-original-message")) message.delete().queue();
                        return;
                    }
                }
                try {
                    final File file = attachment.downloadToFile(downloadedSchematic).get();
                    boolean cancelUpload = false;
                    try {
                        NamedTag nbt = NBTUtil.read(file);
                        if (SchematicAuditUtil.containsBlockedPhrase(nbt)) {
                            sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_CONTAINS_BLACKLISTED_PHRASES.toString()).build()).queue();
                            cancelUpload = true;
                        }
                    } catch (IOException e) {
                        sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_INVALID_SCHEMATIC_FILE.toString()).build()).queue();
                        cancelUpload = true;
                    }

                    if (cancelUpload) {
                        file.delete();
                        if (plugin.getConfig().getBoolean("upload-command-delete-original-message")) message.delete().queue();
                        return;
                    }

                    sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.GREEN, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_SUCCESS.toString(fileName)).build()).queue();
                    plugin.getLogger().info(String.format("User %s (%s) uploaded schematic %s.", message.getAuthor().getAsTag(), message.getAuthor().getId(), fileName));
                } catch (Exception e) {
                    plugin.getLogger().severe(e.getMessage());
                    e.printStackTrace();
                    sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_ERROR.toString()).build()).queue();
                }

                if (plugin.getConfig().getBoolean("upload-command-delete-original-message")) message.delete().queue();
            });
        }
    }
}