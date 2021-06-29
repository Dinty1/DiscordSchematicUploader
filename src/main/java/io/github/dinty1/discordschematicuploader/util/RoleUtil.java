package io.github.dinty1.discordschematicuploader.util;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RoleUtil {
    /**
     * @param resolvable Role name or ID
     * @return The resolved role
     */
    @Nullable
    public static Role resolveToRole(String resolvable) {
        final Guild guild = DiscordSRV.getPlugin().getMainGuild();
        @Nullable Role resolvedRole = null;

        // Try get by Name first
        final List<Role> foundRoles = guild.getRolesByName(resolvable, true);
        if (foundRoles.size() > 0) resolvedRole = foundRoles.get(0);

            // Then try by ID
        else {
            try {
                resolvedRole = guild.getRoleById(resolvable);
            } catch (NumberFormatException ignored) {
            }
        }

        if (resolvedRole != null) return resolvedRole;
        else {
            DiscordSchematicUploader.getPlugin().getLogger().warning(String.format("Could not resolve %s to a role.", resolvable));
            return null;
        }
    }

    /**
     * @param resolvables List of role resolvables (name or ID)
     * @return List of resolved roles
     */
    public static List<Role> resolveToRoles(List<String> resolvables) {
        final List<Role> output = new ArrayList<>();

        for (String resolvable : resolvables) {
            Role resolvedRole = resolveToRole(resolvable);
            if (resolvedRole != null) output.add(resolvedRole);
        }
        return output;
    }

    /**
     * @param member                 The member to check
     * @param allowedRoleResolvables The list of role resolvables to check (name or ID)
     * @return Whether the member has one of these roles
     */
    public static boolean hasAllowedRole(Member member, List<String> allowedRoleResolvables) {
        final List<Role> allowedRoles = resolveToRoles(allowedRoleResolvables);

        if (allowedRoles.contains(member.getGuild().getPublicRole()))
            return true; // User will always have the @everyone role
        else return member.getRoles().stream().anyMatch(allowedRoles::contains);
    }
}
