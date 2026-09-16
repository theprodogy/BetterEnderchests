package com.fernsehheft.enderchest;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Handles the administrator chest-inspection command. */
final class EcSeeCommand {
    private final EnderChest plugin;

    EcSeeCommand(EnderChest plugin) {
        this.plugin = plugin;
    }

    boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("ec.see")) {
            player.sendMessage(this.plugin.getMessage("no-permission"));
            return true;
        }
        if (args.length != 1 && args.length != 2) {
            player.sendMessage(this.plugin.getMessage("usage-ecsee", "<red>ᴜꜱᴀɢᴇ: <dark_purple>/ecsee <player> [page]"));
            return true;
        }

        String targetName = args[0];
        Player onlineTarget = Bukkit.getPlayer(targetName);
        if (onlineTarget != null) {
            this.plugin.handleEcSeeTarget(player, onlineTarget.getUniqueId(), onlineTarget.getName(), args);
            return true;
        }

        this.plugin.resolveOfflinePlayerByName(player, targetName, offlineTarget -> {
            if (!this.plugin.isKnownPlayer(offlineTarget)) {
                player.sendMessage(this.plugin.getMessage("player-never-played").replace("{player}", targetName));
                return;
            }
            String displayName = offlineTarget.getName() == null ? targetName : offlineTarget.getName();
            this.plugin.handleEcSeeTarget(player, offlineTarget.getUniqueId(), displayName, args);
        });
        return true;
    }
}
