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
import io.github.dinty1.discordschematicuploader.util.ConfigUtil;
import io.github.dinty1.discordschematicuploader.util.MessageUtil;
import io.github.dinty1.discordschematicuploader.util.RoleUtil;

import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.List;
import java.util.UUID;

public class DownloadCommand {

    private static final DiscordSchematicUploader plugin = DiscordSchematicUploader.getPlugin();

    // I hate myself for making this static....
    public static void execute(DiscordGuildMessageReceivedEvent event, File schematicFolder, boolean fastAsyncWorldEditEnabled) {
        final Message message = event.getMessage();
        final String downloadCommand = Objects.requireNonNull(plugin.getConfig().getString("download-command"));
        final List<String> flags = MessageUtil.getFlags(event.getMessage(), downloadCommand);

        final String[] splitMessage = message.getContentRaw().split(" ");
        final String schematicName = splitMessage[splitMessage.length - 1]; // Flags first, name at the end

        if (!RoleUtil.hasAllowedRole(event.getMember(), "download-command-allowed-roles")) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.DOWNLOAD_COMMAND_NO_PERMISSION.toString()).build()).queue();
        }
        // Make sure there's a schem name specified
        else if (message.getContentRaw().trim().equals(downloadCommand) || schematicName.startsWith("-")) {
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.DOWNLOAD_COMMAND_NO_NAME_SPECIFIED.toString()).build()).queue();
        } else {
            String schematicFileExtension = ".schem";

            if (fastAsyncWorldEditEnabled) {
                if ((!flags.contains("g") && !plugin.getConfig().getBoolean("download-command-default-to-global")) || flags.contains("p")) {
                    UUID uuid = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(event.getAuthor().getId());
                    if (uuid == null) {
                        message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.NOT_LINKED.toString()).build()).queue();
                        return;
                    }

                    schematicFolder = new File(schematicFolder, uuid.toString());
                } else { // Coming from global so we need to check that user has permission
                    if (!RoleUtil.hasAllowedRole(event.getMember(), "global-download-allowed-roles")) {
                        message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.GLOBAL_DOWNLOAD_NO_PERMISSION.toString()).build()).queue();
                        return;
                    }
                }
            }

            // Make sure that it exists in one form or another
            if (!new File(schematicFolder, schematicName + ".schem").exists()) {
                if (!new File(schematicFolder, schematicName + ".schematic").exists()) {
                    message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.DOWNLOAD_COMMAND_SCHEMATIC_NOT_FOUND.toString(schematicName)).build()).queue();
                    return;
                } else {
                    schematicFileExtension = ".schematic";
                }
            }

            File finalSchematicFolder = schematicFolder;
            String finalSchematicFileExtension = schematicFileExtension;
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.GRAY, message.getAuthor(), ConfigUtil.Message.DOWNLOAD_COMMAND_ATTEMPTING_DOWNLOAD.toString(schematicName + schematicFileExtension)).build()).queue(sentMessage -> {
                final File schematicToDownload = new File(finalSchematicFolder, schematicName + finalSchematicFileExtension);
                try {
                    if (plugin.getConfig().getBoolean("send-downloaded-schematic-privately")) {
                        event.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(ConfigUtil.Message.DOWNLOAD_COMMAND_PRIVATE_MESSAGE.toString(schematicName + finalSchematicFileExtension)).addFile(schematicToDownload).queue(msg -> {
                            sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.GREEN, message.getAuthor(), ConfigUtil.Message.DOWNLOAD_COMMAND_PRIVATE_MESSAGE_SUCCESS.toString(schematicName + finalSchematicFileExtension)).build()).queue();
                            plugin.getLogger().info(String.format("User %s (%s) downloaded schematic %s.", message.getAuthor().getAsTag(), message.getAuthor().getId(), schematicToDownload.getName()));
                        }, t -> notifyDirectMessageError(sentMessage, message)), t -> notifyDirectMessageError(sentMessage, message));
                    } else {
                        message.getChannel().sendMessage(ConfigUtil.Message.DOWNLOAD_COMMAND_DOWNLOAD_MESSAGE.toString()).addFile(schematicToDownload).queue(sentSchematicMessage -> {
                            sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.GREEN, message.getAuthor(), ConfigUtil.Message.DOWNLOAD_COMMAND_SUCCESS.toString(schematicName + finalSchematicFileExtension)).build()).queue();
                            plugin.getLogger().info(String.format("User %s (%s) downloaded schematic %s.", message.getAuthor().getAsTag(), message.getAuthor().getId(), schematicToDownload.getName()));
                        });

                    }
                } catch (IllegalArgumentException e) {
                    sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.DOWNLOAD_COMMAND_FAILED_TO_UPLOAD_TO_DISCORD.toString(schematicName + finalSchematicFileExtension)).build()).queue();
                } catch (Exception e) {
                    plugin.getLogger().severe(e.getMessage());
                    e.printStackTrace();
                    sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.DOWNLOAD_COMMAND_OTHER_ERROR.toString(schematicName + finalSchematicFileExtension)).build()).queue();
                }
            });

        }
    }

    private static void notifyDirectMessageError(Message sentMessage, Message originalMessage) {
        sentMessage.editMessage(MessageUtil.createEmbedBuilder(Color.RED, originalMessage.getAuthor(), ConfigUtil.Message.DOWNLOAD_COMMAND_PRIVATE_MESSAGE_FAILURE.toString()).build()).queue();
    }
}
