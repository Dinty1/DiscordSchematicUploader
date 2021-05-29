package io.github.dinty1.discordschematicuploader.util;

import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
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
}
