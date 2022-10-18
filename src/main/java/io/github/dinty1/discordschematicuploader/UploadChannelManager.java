/*-
 * LICENSE
 * DiscordSchematicUploader
 * -------------
 * Copyright (C) 2021 - 2022 Dinty1
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

package io.github.dinty1.discordschematicuploader;

import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import io.github.dinty1.discordschematicuploader.util.ConfigUtil;
import io.github.dinty1.discordschematicuploader.util.MessageUtil;
import io.github.dinty1.discordschematicuploader.util.RoleUtil;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class UploadChannelManager {

    final DiscordSchematicUploader plugin;

    public UploadChannelManager(final DiscordSchematicUploader plugin) {
        this.plugin = plugin;
    }

    public boolean isUploadChannel(final TextChannel channel) {
        return plugin.getConfig().getStringList("upload-channels").contains(channel.getId());
    }

    public void processMessageInUploadChannel(final Message message, final File schematicFolder) {
        if (message.getMember() == null) return; // Ignore webhooks (I think?)
        if (!MessageUtil.schematicAttached(message)) return;
        if (!RoleUtil.hasAllowedRole(message.getMember(), plugin.getConfig().getStringList("upload-channels-allowed-roles")))
            return;

        final Message.Attachment attachment = message.getAttachments().get(0);

        final String fileName = ConfigUtil.formatSchematicName(attachment, message.getMember());

        // Make sure the schematic doesn't already exist
        final File downloadedSchematic = new File(schematicFolder, fileName);
        final boolean allowedToOverwrite = RoleUtil.hasAllowedRole(message.getMember(), plugin.getConfig().getStringList("upload-command-allowed-to-overwrite"));
        if (downloadedSchematic.exists()) {
            final String overwriteMessage = allowedToOverwrite ? " " + ConfigUtil.Message.UPLOAD_CHANNELS_CAN_OVERWRITE : "";
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_CHANNELS_SCHEMATIC_ALREADY_EXISTS.toString(fileName) + overwriteMessage).build()).queue();
            if (plugin.getConfig().getBoolean("upload-channels-delete-original-message")) message.delete().queue();
            return;
        }
        try {
            final File file = attachment.downloadToFile(downloadedSchematic).get();
            try { // Make sure that the file is valid NBT; Very hacky but it works, will find a more elegant way later
                final NamedTag nbt = NBTUtil.read(file);
            } catch (final IOException e) {
                file.delete();
                if (plugin.getConfig().getBoolean("upload-channels-delete-original-message")) message.delete().queue();
                return;
            }
            plugin.getLogger().info(String.format("User %s (%s) uploaded schematic %s.", message.getAuthor().getAsTag(), message.getAuthor().getId(), fileName));
            message.addReaction("\u2705").queue();
        } catch (final Exception e) {
            plugin.getLogger().severe(e.getMessage());
            e.printStackTrace();
            message.getChannel().sendMessage(MessageUtil.createEmbedBuilder(Color.RED, message.getAuthor(), ConfigUtil.Message.UPLOAD_CHANNELS_UPLOAD_ERROR.toString()).build()).queue();
        }

        if (plugin.getConfig().getBoolean("upload-channels-delete-original-message")) message.delete().queue();
    }
}
