package com.fernsehheft.enderchest;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

/** Handles opening the vanilla-scoreboard team EnderChest. */
final class TeamEnderChestCommand {
    private final EnderChest plugin;

    TeamEnderChestCommand(EnderChest plugin) {
        this.plugin = plugin;
    }

    boolean execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.plugin.getMessage("only-players"));
            return true;
        }
        if (!this.plugin.areTeamsEnabled()) {
            player.sendMessage(this.plugin.getMessage("team-disabled"));
            return true;
        }
        Team team = player.getScoreboard().getEntryTeam(player.getName());
        if (team == null) {
            player.sendMessage(this.plugin.getMessage("team-no-team"));
            return true;
        }
        this.plugin.openEnderChestLogic(player, this.plugin.getTeamChestId(team.getName()), team.getName(), true);
        return true;
    }
}
