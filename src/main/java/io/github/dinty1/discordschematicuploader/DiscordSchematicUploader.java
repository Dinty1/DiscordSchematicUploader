package io.github.dinty1.discordschematicuploader;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.dinty1.discordschematicuploader.listener.DiscordGuildMessagePreProcessListener;
import io.github.dinty1.discordschematicuploader.metrics.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordSchematicUploader extends JavaPlugin {

    private final int METRICS_PLUGIN_ID = 11934;

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, METRICS_PLUGIN_ID);
        saveDefaultConfig();

        DiscordSRV.api.subscribe(new DiscordGuildMessagePreProcessListener(this));
    }

    public static DiscordSchematicUploader getPlugin() {
        return getPlugin(DiscordSchematicUploader.class);
    }
}
