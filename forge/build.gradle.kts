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


import dev.architectury.plugin.ArchitectPluginExtension
import dev.ithundxr.silk.ChangelogText

plugins {
    id("net.neoforged.gradle.userdev") version "7.0.92"
    id("net.neoforged.gradle.mixin") version "7.0.92"
    id("architectury-plugin")
}

architectury {
    // Empty for NeoForge or use platformSetupNeoForge() if available
    injectInjectables = false
}

loom {
    accessWidenerPath.set(project(":common").file("src/main/resources/numismatics.accesswidener"))
    
    runs {
        register("client") {
            client()
            configName = "neo_client"
            ideConfigGenerated(true)
            runDir("run/client")
        }
        register("server") {
            server()
            configName = "neo_server"
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
    implementation("net.neoforged:neoforge:${rootProject.ext["minecraft_version"]}-${rootProject.ext["neoforge_version"]}")
    common(project(":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", configuration = "transformProductionForge")) { isTransitive = false }

    // Create and dependencies
    modImplementation("com.simibubi.create:create-${rootProject.ext["minecraft_version"]}:${rootProject.ext["create_forge_version"]}:slim") { 
        isTransitive = false 
    }
    modImplementation("com.tterrag.registrate:Registrate:${rootProject.ext["registrate_forge_version"]}")
    modImplementation("com.jozufozu.flywheel:flywheel-forge-${rootProject.ext["flywheel_forge_minecraft_version"]}:${rootProject.ext["flywheel_forge_version"]}")

    // EMI
    modLocalRuntime("dev.emi:emi-forge:${rootProject.ext["emi_version"]}")

    // CC:Tweaked
    modCompileOnly("cc.tweaked:cc-tweaked-${rootProject.ext["minecraft_version"]}-forge-api:${rootProject.ext["cc_version"]}")
    modCompileOnly("cc.tweaked:cc-tweaked-${rootProject.ext["minecraft_version"]}-core-api:${rootProject.ext["cc_version"]}")

    // Runtime libraries
    runtimeOnly("cc.tweaked:cobalt:0.9.3")
    runtimeOnly("com.jcraft:jzlib:1.1.3")
    runtimeOnly("io.netty:netty-codec-http:4.1.82.Final")
    runtimeOnly("io.netty:netty-codec-socks:4.1.82.Final")
    runtimeOnly("io.netty:netty-handler-proxy:4.1.82.Final")

    // JEI
    modCompileOnly("mezz.jei:jei-${rootProject.ext["minecraft_version"]}-common-api:${rootProject.ext["jei_version"]}")
    modCompileOnly("mezz.jei:jei-${rootProject.ext["minecraft_version"]}-forge-api:${rootProject.ext["jei_version"]}")
    modLocalRuntime("mezz.jei:jei-${rootProject.ext["minecraft_version"]}-forge:${rootProject.ext["jei_version"]}")

    // Steam 'n' Rails
    val buildNumber = if (rootProject.ext["snr_build_number"] != "null") "-build.${rootProject.ext["snr_build_number"]}" else ""
    modCompileOnly("com.railwayteam.railways:Steam_Rails-forge-${rootProject.ext["minecraft_version"]}:${rootProject.ext["snr_version"]}+forge-mc${rootProject.ext["minecraft_version"]}$buildNumber") { 
        isTransitive = false 
    }
    
    if (rootProject.ext["enable_snr"].toString().toBoolean()) {
        modLocalRuntime("com.railwayteam.railways:Steam_Rails-forge-${rootProject.ext["minecraft_version"]}:${rootProject.ext["snr_version"]}+forge-mc${rootProject.ext["minecraft_version"]}$buildNumber") { 
            isTransitive = false 
        }
    }

    // Mixin Extras
    compileOnly("io.github.llamalad7:mixinextras-common:${rootProject.ext["mixin_extras_version"]}")
    annotationProcessor("io.github.llamalad7:mixinextras-forge:${rootProject.ext["mixin_extras_version"]}")
}

publishMods {
    file = tasks.remapJar.get().archiveFile
    version.set(project.version.toString())
    changelog = ChangelogText.getChangelogText(rootProject).toString()
    type = STABLE
    displayName = "Numismatics ${rootProject.ext["mod_version"]} NeoForge ${rootProject.ext["minecraft_version"]}"
    modLoaders.add("neoforge")

    curseforge {
        projectId = rootProject.ext["curseforge_id"].toString()
        accessToken = System.getenv("CURSEFORGE_TOKEN")
        minecraftVersions.add(rootProject.ext["minecraft_version"].toString())
        requires { slug = "create" }
    }

    modrinth {
        projectId = rootProject.ext["modrinth_id"].toString()
        accessToken = System.getenv("MODRINTH_TOKEN")
        minecraftVersions.add(rootProject.ext["minecraft_version"].toString())
        requires { slug = "create" }
    }
}
