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

package io.github.dinty1.discordschematicuploader.util;

import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;

import java.awt.*;

public class MessageUtil {

    public static EmbedBuilder createEmbedBuilder(Color color, User author, String title) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setAuthor(author.getAsTag(), null, author.getAvatarUrl());
        embedBuilder.setColor(color);
        embedBuilder.setTitle(title);

        return embedBuilder;
    }

    public static boolean schematicAttached(Message message) {
        if (message.getAttachments().size() == 0) return false;
        final Message.Attachment attachment = message.getAttachments().get(0);
        return attachment.getFileExtension() != null && (attachment.getFileExtension().equals("schem") || attachment.getFileExtension().equals("schematic"));
    }
}
