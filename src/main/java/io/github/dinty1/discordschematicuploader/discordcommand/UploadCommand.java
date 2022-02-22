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

import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;
import io.github.dinty1.discordschematicuploader.util.ConfigUtil;
import io.github.dinty1.discordschematicuploader.util.MessageUtil;
import io.github.dinty1.discordschematicuploader.util.RoleUtil;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;

import java.awt.*;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

public class UploadCommand {

    public static void execute(DiscordGuildMessageReceivedEvent event, File schematicFolder) {
        final Message message = event.getMessage();
        final Message.Attachment attachment = message.getAttachments().size() > 0 ? message.getAttachments().get(0) : null;
        final DiscordSchematicUploader plugin = DiscordSchematicUploader.getPlugin();

        if (!RoleUtil.hasAllowedRole(event.getMember(), DiscordSchematicUploader.getPlugin().getConfig().getStringList("upload-command-allowed-roles"))) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_NO_PERMISSION.toString()).build()).queue();
        } else if (attachment == null) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_NO_ATTACHMENT.toString()).build()).queue();
        } else if (attachment.getFileExtension() == null || !(attachment.getFileExtension().equals("schem") || attachment.getFileExtension().equals("schematic"))) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_INVALID_SCHEMATIC_FILE.toString()).build()).queue();
        } else { // Seems legit
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.GRAY, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_ATTEMPTING_SCHEMATIC_SAVE.toString()).build()).queue(sentMessage -> {

                // Make sure the schematic doesn't already exist
                final File downloadedSchematic = new File(schematicFolder, attachment.getFileName());
                final boolean allowedToOverwrite = RoleUtil.hasAllowedRole(event.getMember(), plugin.getConfig().getStringList("upload-command-allowed-to-overwrite"));
                if (downloadedSchematic.exists()) {
                    if (message.getContentRaw().substring(plugin.getConfig().getString("upload-command").length()).contains("-o") && allowedToOverwrite) {
                        downloadedSchematic.delete();
                    } else {
                        final String overwriteMessage = allowedToOverwrite ? " " + ConfigUtil.Message.UPLOAD_COMMAND_CAN_OVERWRITE : "";
                        sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_SCHEMATIC_ALREADY_EXISTS.toString(attachment.getFileName()) + overwriteMessage).build()).queue();
                        if (plugin.getConfig().getBoolean("upload-command-delete-original-message")) message.delete().queue();
                        return;
                    }
                }
                try {
                    final File file = attachment.downloadToFile(downloadedSchematic).get();
                    try { // Make sure that the file is valid NBT; Very hacky but it works, will find a more elegant way later
                        NamedTag nbt = NBTUtil.read(file);
                    } catch (IOException e) {
                        sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_INVALID_SCHEMATIC_FILE.toString()).build()).queue();
                        file.delete();
                        if (plugin.getConfig().getBoolean("upload-command-delete-original-message")) message.delete().queue();
                        return;
                    }
                    sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.GREEN, message.getAuthor(), ConfigUtil.Message.UPLOAD_COMMAND_SUCCESS.toString(attachment.getFileName())).build()).queue();
                    plugin.getLogger().info(String.format("User %s (%s) uploaded schematic %s.", message.getAuthor().getAsTag(), message.getAuthor().getId(), attachment.getFileName()));
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
