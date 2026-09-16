package com.fernsehheft.enderchest;

import java.util.UUID;

/** Immutable parsed representation of one EnderChest invite entry. */
final class InviteAccess {
    final UUID target;
    final boolean allPages;
    final Integer page;

    InviteAccess(UUID target, boolean allPages, Integer page) {
        this.target = target;
        this.allPages = allPages;
        this.page = page;
    }
}
