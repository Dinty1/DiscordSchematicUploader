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
import io.github.dinty1.discordschematicuploader.util.MessageUtil;
import io.github.dinty1.discordschematicuploader.util.RoleUtil;

import java.awt.*;
import java.io.File;

public class UploadCommand {

    public static void execute(DiscordGuildMessageReceivedEvent event, File schematicFolder) {
        final Message message = event.getMessage();
        final Message.Attachment attachment = message.getAttachments().size() > 0 ? message.getAttachments().get(0) : null;
        final DiscordSchematicUploader plugin = DiscordSchematicUploader.getPlugin();

        if (!RoleUtil.hasAllowedRole(event.getMember(), DiscordSchematicUploader.getPlugin().getConfig().getStringList("upload-command-allowed-roles"))) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "You do not have permission to execute this command.").build()).queue();
        } else if (attachment == null) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "You need to attach a file to upload.").build()).queue();
        } else if (attachment.getFileExtension() == null || !(attachment.getFileExtension().equals("schem") || attachment.getFileExtension().equals("schematic"))) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "That's not a valid schematic file.").build()).queue();
        } else { // Seems legit
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.GRAY, message.getAuthor(), "Attempting to save schematic...").build()).queue(sentMessage -> {

                // Make sure the schematic doesn't already exist
                final File downloadedSchematic = new File(schematicFolder, attachment.getFileName());
                final boolean allowedToOverwrite = RoleUtil.hasAllowedRole(event.getMember(), plugin.getConfig().getStringList("upload-command-allowed-to-overwrite"));
                if (downloadedSchematic.exists()) {
                    if (message.getContentRaw().substring(plugin.getConfig().getString("upload-command").length()).contains("-o") && allowedToOverwrite) {
                        downloadedSchematic.delete();
                    } else {
                        final String overwriteMessage = allowedToOverwrite ? " You can replace the old file by adding `-o` to your message when sending the command." : "";
                        sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), "The schematic `" + attachment.getFileName() + "` already exists." + overwriteMessage).build()).queue();
                        return;
                    }
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
    }
}
