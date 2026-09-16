package com.fernsehheft.enderchest;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Handles access to EnderChests shared through the invite system. */
final class EcShareCommand {
    private final EnderChest plugin;

    EcShareCommand(EnderChest plugin) {
        this.plugin = plugin;
    }

    boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.plugin.getMessage("only-players"));
            return true;
        }
        if (!player.hasPermission("ec.share")) {
            player.sendMessage(this.plugin.getMessage("no-permission"));
            return true;
        }
        if (args.length != 1 && args.length != 2) {
            player.sendMessage(this.plugin.getMessage("usage-ecshare"));
            return true;
        }

        String targetName = args[0];
        int page = parsePage(player, args);
        if (page < 0) {
            return true;
        }
        Player onlineTarget = Bukkit.getPlayer(targetName);
        if (onlineTarget != null) {
            this.plugin.handleEcShareTarget(player, onlineTarget.getUniqueId(), onlineTarget.getName(), page);
            return true;
        }

        this.plugin.resolveOfflinePlayerByName(player, targetName, offlineTarget -> {
            if (!offlineTarget.hasPlayedBefore()) {
                player.sendMessage(this.plugin.getMessage("player-never-played").replace("{player}", targetName));
                return;
            }
            this.plugin.handleEcShareTarget(player, offlineTarget.getUniqueId(), offlineTarget.getName(), page);
        });
        return true;
    }

    private int parsePage(Player player, String[] args) {
        if (args.length == 1) {
            return 1;
        }
        if (!this.plugin.arePagesEnabled()) {
            player.sendMessage(this.plugin.prefixed("<red>ᴘᴀɢᴇꜱ ᴀʀᴇ ᴅɪꜱᴀʙʟᴇᴅ."));
            return -1;
        }
        try {
            return Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            player.sendMessage(this.plugin.prefixed("<red>ᴘᴀɢᴇ ᴍᴜꜱᴛ ʙᴇ ᴀ ɴᴜᴍʙᴇʀ."));
            return -1;
        }
    }
}
