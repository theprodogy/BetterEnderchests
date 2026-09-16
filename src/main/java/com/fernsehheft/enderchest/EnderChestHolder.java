package com.fernsehheft.enderchest;

import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class EnderChestHolder
implements InventoryHolder {
    private final UUID ownerId;
    private final String ownerName;
    private final int page;
    private final long generation;
    private final boolean team;
    private final boolean paged;
    private Inventory inventory;

    EnderChestHolder(UUID ownerId, String ownerName, int page, long generation, boolean team, boolean paged) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.page = page;
        this.generation = generation;
        this.team = team;
        this.paged = paged;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public int getPage() {
        return this.page;
    }

    public long getGeneration() {
        return this.generation;
    }

    public boolean isTeam() {
        return this.team;
    }

    public boolean isPaged() {
        return this.paged;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
