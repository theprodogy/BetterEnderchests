package com.fernsehheft.enderchest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/** Dedicated event boundary for virtual EnderChest inventory interactions. */
final class EnderChestInventoryListener implements Listener {
    private final EnderChest plugin;

    EnderChestInventoryListener(EnderChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    void onInventoryClick(InventoryClickEvent event) {
        this.plugin.handleInventoryClick(event);
    }

    @EventHandler
    void onInventoryDrag(InventoryDragEvent event) {
        this.plugin.handleInventoryDrag(event);
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event) {
        this.plugin.handleInventoryClose(event);
    }
}
