plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
}

group = "com.fernsehheft"
version = "2.4.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
    maven("https://repo.codemc.io/repository/maven-public/") {
        name = "codemc"
    }
}

dependencies {
    // Built against the oldest supported API (1.21.11); runs unchanged on 26.1-26.3.
    // `-PpaperApi=26.3.build.223-stable -PjavaRelease=25` compile-checks against the newest API.
    compileOnly("io.papermc.paper:paper-api:${findProperty("paperApi") ?: "1.21.11-R0.1-SNAPSHOT"}")
    implementation("de.tr7zw:item-nbt-api:2.16.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        // 1.21.11 servers run Java 21, 26.x needs Java 25; release 21 bytecode loads on both.
        options.release.set((findProperty("javaRelease") as String? ?: "21").toInt())
        options.compilerArgs.addAll(listOf("-Xlint:all"))
    }

    processResources {
        filteringCharset = "UTF-8"
    }

    shadowJar {
        archiveClassifier.set("")
        archiveVersion.set("")
        relocate("de.tr7zw.changeme.nbtapi", "com.fernsehheft.enderchest.nbtapi")
    }

    jar {
        archiveClassifier.set("plain")
    }

    build {
        dependsOn(shadowJar)
    }
}
