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

package io.github.dinty1.discordschematicuploader;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.dinty1.discordschematicuploader.listener.DiscordMessageListener;
import io.github.dinty1.discordschematicuploader.listener.PlayerJoinListener;
import io.github.dinty1.discordschematicuploader.metrics.Metrics;
import io.github.dinty1.discordschematicuploader.minecraftcommand.AdminCommand;
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

        getCommand("discordschematicuploader").setExecutor(new AdminCommand(this));

        DiscordSRV.api.subscribe(new DiscordMessageListener(this));
    }
}
