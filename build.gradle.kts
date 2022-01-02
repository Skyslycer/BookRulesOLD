plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.1"
}

group = "de.skyslycer"
version = "2.3.3"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://mvn.intellectualsites.com/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.10.10")
    compileOnly("io.netty:netty-all:4.1.72.Final")
    implementation("mysql:mysql-connector-java:8.0.27")
    implementation("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:2.2.1")
    implementation("net.kyori:adventure-platform-bungeecord:4.0.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    shadowJar {
        relocate("org.bstats", "de.skyslycer.shade.bstats")
        minimize()
        classifier = null
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        from(sourceSets.main.get().resources.srcDirs) {
            expand(
                "version" to project.version,
                "name" to project.name
            )
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}