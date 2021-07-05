package io.github.dinty1.discordschematicuploader.listener;

import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("discordschematicuploader.adminalerts") && DiscordSchematicUploader.getPlugin().isUpdateAvailable())
            event.getPlayer().sendMessage(ChatColor.GOLD + "An update for DiscordSchematicUploader is available! Get it here: https://github.com/Dinty1/DiscordSchematicUploader/releases");
    }
}
