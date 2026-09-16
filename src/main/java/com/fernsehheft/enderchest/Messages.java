package com.fernsehheft.enderchest;

import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * The single text boundary for EnderChest.
 *
 * <p>All new messages use MiniMessage. The legacy serializers exist only to
 * migrate old configuration values that used '&' or section-sign formatting.</p>
 */
final class Messages {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private Messages() {
    }

    static Component parse(String template, Map<String, String> placeholders) {
        TagResolver[] resolvers = placeholders.entrySet().stream()
            .map(entry -> Placeholder.unparsed(entry.getKey(), entry.getValue()))
            .toArray(TagResolver[]::new);
        return MINI_MESSAGE.deserialize(template, resolvers);
    }

    static Component parse(String template) {
        return MINI_MESSAGE.deserialize(template);
    }

    static Component parseCompatible(String template) {
        return containsLegacyFormatting(template) ? parse(migrateLegacy(template)) : parse(template);
    }

    static Component prefixed(String prefix, String template, Map<String, String> placeholders) {
        return parse(prefix + template, placeholders);
    }

    static String plain(String template) {
        return PlainTextComponentSerializer.plainText().serialize(parseCompatible(template));
    }

    /** Temporary bridge for unchanged String-only Bukkit APIs during migration. */
    static String legacy(String template) {
        return LegacyComponentSerializer.legacySection().serialize(parseCompatible(template));
    }

    /** Converts a legacy string into its equivalent MiniMessage representation. */
    static String migrateLegacy(String value) {
        Component component = value.indexOf('\u00a7') >= 0
            ? LegacyComponentSerializer.legacySection().deserialize(value)
            : LegacyComponentSerializer.legacyAmpersand().deserialize(value);
        return MINI_MESSAGE.serialize(component);
    }

    /**
     * Converts legacy strings in a config once. Already-MiniMessage values are
     * untouched, making this safe to call during every plugin startup.
     */
    static boolean migrateLegacyValues(FileConfiguration configuration) {
        boolean changed = false;
        for (String path : configuration.getKeys(true)) {
            if (!configuration.isString(path)) continue;
            String value = configuration.getString(path, "");
            if (!containsLegacyFormatting(value)) continue;
            configuration.set(path, migrateLegacy(value));
            changed = true;
        }
        return changed;
    }

    static boolean containsLegacyFormatting(String value) {
        if (value.indexOf('\u00a7') >= 0) return true;
        for (int index = 0; index + 1 < value.length(); index++) {
            if (value.charAt(index) != '&') continue;
            if ("0123456789abcdefklmnorABCDEFKLMNOR".indexOf(value.charAt(index + 1)) >= 0) return true;
        }
        return false;
    }
}
