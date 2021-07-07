package io.github.dinty1.discordschematicuploader.util;

import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;

import java.util.List;

public class RoleUtil {

    /**
     * @param member                 The member to check
     * @param allowedRoleResolvables The list of role resolvables to check (name or ID)
     * @return Whether the member has one of these roles
     */
    public static boolean hasAllowedRole(Member member, List<String> allowedRoleResolvables) {
        if (allowedRoleResolvables.contains("@everyone")) return true; // @everyone has permission, no need to do anything else
        else {
            return member.getRoles().stream().anyMatch(r -> allowedRoleResolvables.contains(r.getName()) || allowedRoleResolvables.contains(r.getId()));
        }
    }
}
