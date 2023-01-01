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

import github.scarsz.discordsrv.DiscordSRV;
import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ConfigUtil {

    public static void migrateIfNeeded(FileConfiguration oldConfig, Plugin plugin) throws IOException {
        if (!oldConfig.getString("config-version").equals(plugin.getDescription().getVersion())) {
            plugin.getLogger().info("Your config version does not match the plugin version, updating...");

            //load config
            File oldConfigFile = new File(plugin.getDataFolder(), "config.yml");

            //put values in map
            Scanner oldConfigReader = new Scanner(oldConfigFile);
            Map<String, String> oldConfigMap = new HashMap<>();
            while (oldConfigReader.hasNextLine()) {
                final String line = oldConfigReader.nextLine();
                if (line.startsWith("#")) continue;
                final String[] split = line.split(":");
                if (split.length != 2) continue;
                oldConfigMap.put(split[0], split[1].trim());

            }
            oldConfigReader.close();

            //load new config
            oldConfigFile.delete();
            plugin.saveDefaultConfig();

            File newConfigFile = new File(plugin.getDataFolder(), "config.yml");

            //change values where necessary
            Scanner newConfigReader = new Scanner(newConfigFile);
            final List<String> newConfigLines = new ArrayList<>();
            while (newConfigReader.hasNextLine()) {
                final String line = newConfigReader.nextLine();
                newConfigLines.add(line);
                if (line.startsWith("config-version") || line.startsWith("#")) continue;
                final String[] split = line.split(":");
                if (split.length != 2) continue;
                if (oldConfigMap.containsKey(split[0])) {
                    split[1] = oldConfigMap.get(split[0]);
                    newConfigLines.set(newConfigLines.size() - 1, String.join(": ", split));
                    plugin.getLogger().info("Migrated config option " + split[0] + " with value " + split[1]);
                }
            }
            final String newConfig = String.join(System.lineSeparator(), newConfigLines);
            FileWriter fileWriter = new FileWriter(new File(plugin.getDataFolder(), "config.yml"));
            fileWriter.write(newConfig);
            fileWriter.close();

            plugin.reloadConfig();
        }
    }

    public static String formatSchematicName(github.scarsz.discordsrv.dependencies.jda.api.entities.Message.Attachment attachment, Member member) {
        @Nullable OfflinePlayer player = null;
        final String filename = FileUtil.removeFileExtension(attachment.getFileName());
        final String extension = attachment.getFileExtension();

        // Try find a linked player
        final @Nullable UUID uuid = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(member.getId());
        if (uuid != null && Bukkit.getServer().getOfflinePlayer(uuid).getName() != null) {
            player = Bukkit.getServer().getOfflinePlayer(uuid);
        }
        return DiscordSchematicUploader.getPlugin().getConfig().getString("schematic-name-format")
                .replace("%filename%", filename)
                .replace("%discordname%", member.getEffectiveName())
                .replace("%discordusername%", member.getUser().getName())
                .replace("%discorduserdiscrim%", member.getUser().getDiscriminator())
                .replace("%minecraftusername%", player == null ? "" : player.getName())
                .replace("%minecraftuuid%", uuid == null ? "" : uuid.toString())
                .replace("%minecraftordiscordname%", player == null ? member.getEffectiveName() : player.getName())
                .replace("%minecraftordiscordusername%", player == null ? member.getUser().getName() : player.getName())
                + "." + extension;
    }

    public enum Message {
        UPLOAD_COMMAND_NO_PERMISSION("upload-command-no-permission"),
        UPLOAD_COMMAND_NO_ATTACHMENT("upload-command-no-attachment"),
        UPLOAD_COMMAND_INVALID_SCHEMATIC_FILE("upload-command-invalid-schematic-file"),
        UPLOAD_COMMAND_CONTAINS_BLACKLISTED_PHRASES("upload-command-contains-blacklisted-phrases"),
        UPLOAD_COMMAND_ATTEMPTING_SCHEMATIC_SAVE("upload-command-attempting-schematic-save"),
        UPLOAD_COMMAND_SCHEMATIC_ALREADY_EXISTS("upload-command-schematic-already-exists"),
        UPLOAD_COMMAND_CAN_OVERWRITE("upload-command-can-overwrite"),
        UPLOAD_COMMAND_SUCCESS("upload-command-success"),
        UPLOAD_COMMAND_ERROR("upload-command-error"),

        UPLOAD_CHANNELS_SCHEMATIC_ALREADY_EXISTS("upload-channels-schematic-already-exists"),
        UPLOAD_CHANNELS_CAN_OVERWRITE("upload-channels-can-overwrite"),
        UPLOAD_CHANNELS_UPLOAD_ERROR("upload-channels-upload-error"),

        DOWNLOAD_COMMAND_NO_PERMISSION("download-command-no-permission"),
        DOWNLOAD_COMMAND_NO_NAME_SPECIFIED("download-command-no-name-specified"),
        DOWNLOAD_COMMAND_SCHEMATIC_NOT_FOUND("download-command-schematic-not-found"),
        DOWNLOAD_COMMAND_ATTEMPTING_DOWNLOAD("download-command-attempting-download"),
        DOWNLOAD_COMMAND_PRIVATE_MESSAGE("download-command-private-message"),
        DOWNLOAD_COMMAND_PRIVATE_MESSAGE_SUCCESS("download-command-private-message-success"),
        DOWNLOAD_COMMAND_PRIVATE_MESSAGE_FAILURE("download-command-private-message-failure"),
        DOWNLOAD_COMMAND_DOWNLOAD_MESSAGE("download-command-download-message"),
        DOWNLOAD_COMMAND_SUCCESS("download-command-success"),
        DOWNLOAD_COMMAND_FAILED_TO_UPLOAD_TO_DISCORD("download-command-failed-to-upload-to-discord"),
        DOWNLOAD_COMMAND_OTHER_ERROR("download-command-other-error");


        private final String configOption;

        Message(String configOption) {
            this.configOption = configOption;
        }

        @Override
        public String toString() {
            final String returnValue = DiscordSchematicUploader.getPlugin().getConfig().getString(this.configOption);
            return returnValue.length() > 256 ? returnValue.substring(0, 255) : returnValue;
        }

        public String toString(String schematicName) {
            return this.toString().replace("%schematic%", schematicName);
        }
    }
}
