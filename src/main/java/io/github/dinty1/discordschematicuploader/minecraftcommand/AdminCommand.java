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

    public AdminCommand(final DiscordSchematicUploader plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args) {
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
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args) {
        if (args.length == 1) return Arrays.asList(subCommands);
        else return Collections.emptyList();
    }
}
