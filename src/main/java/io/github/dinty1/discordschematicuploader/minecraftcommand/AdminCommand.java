package io.github.dinty1.discordschematicuploader.minecraftcommand;

import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final String[] subCommands = new String[]{"reloadconfig"};
    private final DiscordSchematicUploader plugin;

    public AdminCommand(DiscordSchematicUploader plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length < 1) return false;

        switch (args[0]) {
            case "reloadconfig":
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GOLD + "Config reloaded!");
                break;
            default:
                return false;
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) return Arrays.asList(subCommands);
        else return Collections.emptyList();
    }
}
