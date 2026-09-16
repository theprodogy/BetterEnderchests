/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.folialib.enums;

import com.fernsehheft.enderchest.folialib.util.ImplementationTestsUtil;
import java.util.function.Supplier;

public enum ImplementationType {
    FOLIA("FoliaImplementation", new Supplier[0], "io.papermc.paper.threadedregions.RegionizedServer"),
    PAPER("PaperImplementation", new Supplier[]{ImplementationTestsUtil::isTaskConsumersSupported}, "com.destroystokyo.paper.PaperConfig", "io.papermc.paper.configuration.Configuration"),
    LEGACY_PAPER("LegacyPaperImplementation", new Supplier[0], "com.destroystokyo.paper.PaperConfig", "io.papermc.paper.configuration.Configuration"),
    SPIGOT("SpigotImplementation", new Supplier[]{ImplementationTestsUtil::isTaskConsumersSupported}, "org.spigotmc.SpigotConfig"),
    LEGACY_SPIGOT("LegacySpigotImplementation", new Supplier[0], "org.spigotmc.SpigotConfig"),
    UNKNOWN("UnsupportedImplementation", new Supplier[0], new String[0]);

    private final String implementationClassName;
    private final Supplier<Boolean>[] tests;
    private final String[] classNames;

    private ImplementationType(String implementationClassName, Supplier<Boolean>[] tests, String ... classNames) {
        this.implementationClassName = implementationClassName;
        this.tests = tests;
        this.classNames = classNames;
    }

    public String getImplementationClassName() {
        return this.implementationClassName;
    }

    public Supplier<Boolean>[] getTests() {
        return this.tests;
    }

    public String[] getClassNames() {
        return this.classNames;
    }

    public boolean selfCheck() {
        String[] classNames;
        for (Supplier<Boolean> test : this.getTests()) {
            if (test.get().booleanValue()) continue;
            return false;
        }
        for (String className : classNames = this.getClassNames()) {
            try {
                Class.forName(className);
                return true;
            }
            catch (ClassNotFoundException classNotFoundException) {
            }
        }
        return false;
    }
}

