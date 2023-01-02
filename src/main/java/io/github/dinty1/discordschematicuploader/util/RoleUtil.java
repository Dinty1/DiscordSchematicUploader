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

import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;

import java.util.List;

public class RoleUtil {

    /**
     * @param member       The member to check
     * @param configOption The config option of role resolvables to check (name or ID)
     * @return Whether the member has one of these roles
     */
    public static boolean hasAllowedRole(Member member, String configOption) {
        final List<String> allowedRoleResolvables = DiscordSchematicUploader.getPlugin().getConfig().getStringList(configOption);
        if (allowedRoleResolvables.contains("@everyone")) return true; // @everyone has permission, no need to do anything else
        else {
            return member.getRoles().stream().anyMatch(r -> allowedRoleResolvables.contains(r.getName()) || allowedRoleResolvables.contains(r.getId()));
        }
    }
}
