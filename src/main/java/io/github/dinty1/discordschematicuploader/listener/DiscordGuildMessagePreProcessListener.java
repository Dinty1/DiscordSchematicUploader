package io.github.dinty1.discordschematicuploader.listener;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;
import io.github.dinty1.discordschematicuploader.command.DownloadCommand;
import io.github.dinty1.discordschematicuploader.command.UploadCommand;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.Objects;

public class DiscordGuildMessagePreProcessListener {
    private final DiscordSchematicUploader plugin;

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

        // If it's the upload command
        if (message.getContentRaw().startsWith(uploadCommand)) UploadCommand.execute(event, schematicFolder);
        else if (message.getContentRaw().startsWith(downloadCommand)) DownloadCommand.execute(event, schematicFolder);
    }
}
