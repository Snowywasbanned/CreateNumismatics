/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    id("net.neoforged.gradle.userdev") version "7.0.92" // Updated version
    id("net.neoforged.gradle.mixin") version "7.0.92"
}

architectury {
    // NeoForge doesn't use architectury.forge() - use this instead:
    platformSetupNeoForge()
    injectInjectables = false
}

loom {
    accessWidenerPath.set(project(":common").file("src/main/resources/numismatics.accesswidener"))
    
    runs {
        client {
            client()
            ideConfigGenerated(true)
            runDir("run/client")
        }
        server {
            server()
            ideConfigGenerated(true)
            runDir("run/server")
        }
    }
    
    mixin {
        defaultRefmapName.set("numismatics.refmap.json")
        add(sourceSets.main.get(), "numismatics.mixins.refmap.json")
    }
}

repositories {
    maven("https://api.modrinth.com/maven")
    maven("https://maven.theillusivec4.top/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://jitpack.io/")
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.tterrag.com/") {
        content {
            includeGroup("com.tterrag.registrate")
            includeGroup("com.simibubi.create")
        }
    }
    maven("https://squiddev.cc/maven/") {
        content {
            includeGroup("cc.tweaked")
        }
    }
    maven("https://maven.blamejared.com/") {
        content {
            includeGroup("mezz.jei")
        }
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${"minecraft_version"()}-${"neoforge_version"()}")
    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionForge")) { isTransitive = false }

    // Create and dependencies - check for NeoForge versions
    modImplementation("com.simibubi.create:create-${"minecraft_version"()}:${"create_forge_version"()}:slim") { 
        isTransitive = false 
    }
    modImplementation("com.tterrag.registrate:Registrate:${"registrate_forge_version"()}")
    modImplementation("com.jozufozu.flywheel:flywheel-forge-${"flywheel_forge_minecraft_version"()}:${"flywheel_forge_version'()}")

    // Update EMI to NeoForge version when available
    modLocalRuntime("dev.emi:emi-forge:${"emi_version"()}")

    // CC:Tweaked - check for NeoForge support
    modCompileOnly("cc.tweaked:cc-tweaked-${"minecraft_version"()}-forge-api:${"cc_version"()}")
    modCompileOnly("cc.tweaked:cc-tweaked-${"minecraft_version"()}-core-api:${"cc_version"()}")

    // Runtime libraries
    runtimeOnly("cc.tweaked:cobalt:0.9.3")
    runtimeOnly("com.jcraft:jzlib:1.1.3")
    runtimeOnly("io.netty:netty-codec-http:4.1.82.Final")
    runtimeOnly("io.netty:netty-codec-socks:4.1.82.Final")
    runtimeOnly("io.netty:netty-handler-proxy:4.1.82.Final")

    // JEI - consider switching to EMI or other alternatives
    modCompileOnly("mezz.jei:jei-${"minecraft_version"()}-common-api:${"jei_version"()}")
    modCompileOnly("mezz.jei:jei-${"minecraft_version"()}-forge-api:${"jei_version"()}")
    modLocalRuntime("mezz.jei:jei-${"minecraft_version"()}-forge:${"jei_version'()}")

    // Other mod dependencies
    val buildNumber = if ("snr_build_number"() != "null") "-build." + "snr_build_number"() else ""
    modCompileOnly("com.railwayteam.railways:Steam_Rails-forge-${"minecraft_version"()}:${"snr_version"()}+forge-mc${"minecraft_version"() + buildNumber}") { 
        isTransitive = false 
    }
    
    if ("enable_snr"().toBoolean()) {
        modLocalRuntime("com.railwayteam.railways:Steam_Rails-forge-${"minecraft_version"()}:${"snr_version"()}+forge-mc${"minecraft_version"() + buildNumber}") { 
            isTransitive = false 
        }
    }

    // Mixin Extras
    compileOnly("io.github.llamalad7:mixinextras-common:${"mixin_extras_version"()}")
    annotationProcessor("io.github.llamalad7:mixinextras-forge:${"mixin_extras_version"()}")
}

publishMods {
    file = tasks.remapJar.get().archiveFile
    version.set(project.version.toString())
    changelog = ChangelogText.getChangelogText(rootProject).toString()
    type = STABLE
    displayName = "Numismatics ${"mod_version"()} NeoForge ${"minecraft_version"()}"
    modLoaders.add("neoforge")

    curseforge {
        projectId = "curseforge_id"()
        accessToken = System.getenv("CURSEFORGE_TOKEN")
        minecraftVersions.add("minecraft_version"())
        requires { slug = "create" }
    }

    modrinth {
        projectId = "modrinth_id"()
        accessToken = System.getenv("MODRINTH_TOKEN")
        minecraftVersions.add("minecraft_version"())
        requires { slug = "create" }
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
