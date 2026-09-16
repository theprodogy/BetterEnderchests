package com.fernsehheft.enderchest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

/** Stable binary codec used by file storage, SQL storage, backups, and repair. */
final class ItemStorageCodec {
    private ItemStorageCodec() {
    }

    static String encode(ItemStack[] items) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); BukkitObjectOutputStream output = new BukkitObjectOutputStream(bytes)) {
            output.writeInt(items.length);
            for (ItemStack item : items) {
                output.writeObject(item);
            }
            return Base64.getEncoder().encodeToString(bytes.toByteArray());
        }
    }

    static ItemStack[] decode(String data) throws IOException, ClassNotFoundException {
        if (data == null || data.isEmpty()) {
            return new ItemStack[0];
        }
        try (BukkitObjectInputStream input = new BukkitObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(data)))) {
            ItemStack[] items = new ItemStack[input.readInt()];
            for (int index = 0; index < items.length; index++) {
                items[index] = (ItemStack) input.readObject();
            }
            return items;
        }
    }
}
