package io.github.dinty1.discordschematicuploader;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.dinty1.discordschematicuploader.listener.DiscordGuildMessagePreProcessListener;
import io.github.dinty1.discordschematicuploader.metrics.Metrics;
import io.github.dinty1.discordschematicuploader.util.ConfigUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class DiscordSchematicUploader extends JavaPlugin {

    private final int METRICS_PLUGIN_ID = 11934;

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, METRICS_PLUGIN_ID);

        saveDefaultConfig();
        try {
            ConfigUtil.migrateIfNeeded(getConfig(), this);
        } catch (IOException e) {
            getLogger().severe("An error occurred while attempting to migrate the configuration: " + e.getMessage());
            e.printStackTrace();
        }

        DiscordSRV.api.subscribe(new DiscordGuildMessagePreProcessListener(this));
    }

    public static DiscordSchematicUploader getPlugin() {
        return getPlugin(DiscordSchematicUploader.class);
    }
}
