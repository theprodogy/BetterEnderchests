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
    // Java 25 is the project's selected runtime target.
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("de.tr7zw:item-nbt-api:2.16.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(25)
        options.compilerArgs.addAll(listOf("-Xlint:all"))
    }

    processResources {
        filteringCharset = "UTF-8"
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("de.tr7zw.changeme.nbtapi", "com.fernsehheft.enderchest.nbtapi")
    }

    jar {
        archiveClassifier.set("plain")
    }

    build {
        dependsOn(shadowJar)
    }
}
