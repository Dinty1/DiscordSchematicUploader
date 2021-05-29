package io.github.dinty1.discordschematicuploader;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.dinty1.discordschematicuploader.listener.DiscordGuildMessagePreProcessListener;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordSchematicUploader extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        DiscordSRV.api.subscribe(new DiscordGuildMessagePreProcessListener(this));
    }

    public static DiscordSchematicUploader getPlugin() {
        return getPlugin(DiscordSchematicUploader.class);
    }
}
