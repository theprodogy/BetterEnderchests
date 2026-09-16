package com.fernsehheft.enderchest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/** Owns invite parsing, persistence, and page-scoped access checks. */
final class InviteService {
    private final EnderChest plugin;

    InviteService(EnderChest plugin) {
        this.plugin = plugin;
    }

    InviteAccess parse(String entry) {
        if (entry == null || entry.isBlank()) return null;
        try {
            String[] parts = entry.split("\\|", 2);
            UUID target = UUID.fromString(parts[0]);
            if (parts.length == 1) return new InviteAccess(target, false, 1);
            String scope = parts[1].trim().toLowerCase(Locale.ROOT);
            if (scope.equals("all")) return new InviteAccess(target, true, null);
            if (scope.startsWith("page:")) return new InviteAccess(target, false, Math.max(1, Integer.parseInt(scope.substring(5))));
            return new InviteAccess(target, false, 1);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    List<String> entries(UUID owner) {
        return new ArrayList<>(this.plugin.getInvitesConfiguration().getStringList(owner.toString()));
    }

    boolean hasAccess(UUID owner, UUID viewer, Integer requestedPage) {
        if (owner.equals(viewer)) return true;
        for (String entry : entries(owner)) {
            InviteAccess access = parse(entry);
            if (access == null || !access.target.equals(viewer)) continue;
            if (requestedPage == null || !this.plugin.arePagesEnabled() || access.allPages || access.page != null && access.page.equals(requestedPage)) return true;
        }
        return false;
    }

    List<String> descriptions(UUID owner) {
        List<String> result = new ArrayList<>();
        for (String entry : entries(owner)) {
            InviteAccess access = parse(entry);
            if (access == null) continue;
            OfflinePlayer player = Bukkit.getOfflinePlayer(access.target);
            String name = player.getName() == null ? access.target.toString().substring(0, 8) : player.getName();
            result.add(!this.plugin.arePagesEnabled() ? name : access.allPages ? name + " (all pages)" : name + " (page " + (access.page == null ? 1 : access.page) + ")");
        }
        return result;
    }

    boolean save(UUID owner, UUID target, Integer page, boolean allPages) {
        List<String> entries = entries(owner);
        for (String entry : entries) {
            InviteAccess existing = parse(entry);
            if (existing == null || !existing.target.equals(target)) continue;
            if (existing.allPages && allPages) return false;
            if (!existing.allPages && !allPages && (existing.page == null ? 1 : existing.page) == (page == null ? 1 : page)) return false;
        }
        entries.removeIf(entry -> { InviteAccess access = parse(entry); return access != null && access.target.equals(target); });
        entries.add(allPages ? target + "|all" : page == null || page <= 1 ? target.toString() : target + "|page:" + page);
        return write(owner, entries);
    }

    void remove(UUID owner, UUID target) {
        List<String> entries = entries(owner);
        if (entries.removeIf(entry -> { InviteAccess access = parse(entry); return access != null && access.target.equals(target); })) write(owner, entries);
    }

    private boolean write(UUID owner, List<String> entries) {
        this.plugin.getInvitesConfiguration().set(owner.toString(), entries);
        try {
            this.plugin.getInvitesConfiguration().save(this.plugin.getInvitesFile());
            return true;
        } catch (IOException exception) {
            this.plugin.getLogger().warning("Could not save invites: " + exception.getMessage());
            return false;
        }
    }
}
