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

package io.github.dinty1.discordschematicuploader.listener;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;
import io.github.dinty1.discordschematicuploader.discordcommand.DownloadCommand;
import io.github.dinty1.discordschematicuploader.discordcommand.UploadCommand;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.Objects;

public class DiscordMessageListener {
    private final String uploadCommand;
    private final String downloadCommand;

    public DiscordMessageListener(DiscordSchematicUploader plugin) {
        this.uploadCommand = Objects.requireNonNull(plugin.getConfig().getString("upload-command"));
        this.downloadCommand = Objects.requireNonNull(plugin.getConfig().getString("download-command"));
    }

    private boolean isValidCommand(String message) {
        return message.startsWith(uploadCommand) || message.startsWith(downloadCommand);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onDiscordMessageInLinkedChannel(DiscordGuildMessagePreProcessEvent event) {
        if (isValidCommand(event.getMessage().getContentRaw())) event.setCancelled(true);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onDiscordMessage(DiscordGuildMessageReceivedEvent event) {
        // None of this sneaky adding the bot to another server to bypass allowed role checks shit thank you very much
        if (event.getGuild() != DiscordSRV.getPlugin().getMainGuild()) return;

        final File worldeditDataFolder = Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("WorldEdit")).getDataFolder();
        final File schematicFolder = new File(worldeditDataFolder, "schematics");

        Message message = event.getMessage();

        // If it's the upload command
        if (message.getContentRaw().startsWith(uploadCommand)) UploadCommand.execute(event, schematicFolder);
        else if (message.getContentRaw().startsWith(downloadCommand)) DownloadCommand.execute(event, schematicFolder);
    }
}
