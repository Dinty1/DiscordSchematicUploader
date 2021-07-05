package io.github.dinty1.discordschematicuploader.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ConfigUtil {

    public static void migrateIfNeeded(FileConfiguration oldConfig, Plugin plugin) throws IOException {
        if (!oldConfig.getString("config-version").equals(plugin.getDescription().getVersion())) {
            plugin.getLogger().info("Your config version does not match the plugin version, migrating...");

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
}
