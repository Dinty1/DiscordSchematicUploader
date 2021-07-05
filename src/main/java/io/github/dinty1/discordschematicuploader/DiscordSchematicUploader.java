package io.github.dinty1.discordschematicuploader;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.dinty1.discordschematicuploader.listener.DiscordGuildMessagePreProcessListener;
import io.github.dinty1.discordschematicuploader.listener.PlayerJoinListener;
import io.github.dinty1.discordschematicuploader.metrics.Metrics;
import io.github.dinty1.discordschematicuploader.util.ConfigUtil;
import io.github.dinty1.discordschematicuploader.util.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class DiscordSchematicUploader extends JavaPlugin {

    private boolean updateAvailable = false;

    public static DiscordSchematicUploader getPlugin() {
        return getPlugin(DiscordSchematicUploader.class);
    }

    public boolean isUpdateAvailable() {
        return this.updateAvailable;
    }

    @Override
    public void onEnable() {
        int METRICS_PLUGIN_ID = 11934;
        Metrics metrics = new Metrics(this, METRICS_PLUGIN_ID);

        saveDefaultConfig();
        try {
            ConfigUtil.migrateIfNeeded(getConfig(), this);
        } catch (IOException e) {
            getLogger().severe("An error occurred while attempting to migrate the configuration: " + e.getMessage());
            e.printStackTrace();
        }
        if (getConfig().getBoolean("update-check")) {
            getLogger().info("Checking for updates...");
            new UpdateChecker(this, 93925).getLatestVersion(v -> {
                if (this.getDescription().getVersion().equalsIgnoreCase(v)) {
                    getLogger().info("Up to date!");
                } else {
                    getLogger().info("An update is available! Get it here: https://github.com/Dinty1/DiscordSchematicUploader/releases");
                    this.updateAvailable = true;
                }
            });
        }

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        DiscordSRV.api.subscribe(new DiscordGuildMessagePreProcessListener(this));
    }
}
