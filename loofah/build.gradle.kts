import net.fabricmc.loom.task.AbstractRunTask
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RunGameTask

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") {
        name = "Fabric"
    }
    maven("https://maven.parchmentmc.org") {
        name = "ParchementMC"
    }
    maven("https://repo.spongepowered.org/repository/maven-public/") {
        name = "SpongePowered"
    }
}

plugins {
    id("fabric-loom") version "1.6.3"
    id("com.github.johnrengelman.shadow")
    id("implementation-structure")
    id("maven-publish")
}

val commonProject = parent!!
val testPluginsProject: Project? = rootProject.subprojects.find { "testplugins" == it.name }
val apiJavaTarget: String by project
val apiVersion: String by project
val minecraftVersion: String by project
val parchmentBuild: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val secureModulesVersion: String by project
val recommendedVersion: String by project
val organization: String by project
val projectUrl: String by project

version = spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion, fabricLoaderVersion)

// Common source sets and configurations
val main: SourceSet = commonProject.sourceSets.named("main").get()
val launch: SourceSet = commonProject.sourceSets.named("launch").get()
val launchConfig: Configuration = commonProject.configurations.named("launch").get()
val applaunch: SourceSet = commonProject.sourceSets.named("applaunch").get()
val mixins: SourceSet = commonProject.sourceSets.named("mixins").get()
val accessors: SourceSet = commonProject.sourceSets.named("accessors").get()


//Fabric source sets and configurations
val gameManagedLibraries = configurations.register("gameManagedLibraries").get()
// Kinda hackish but makes the game itself dependable.
afterEvaluate {
    gameManagedLibraries.extendsFrom(configurations.named("minecraftNamedCompile").get())
}
val fabricBootstrapLibrariesConfig = configurations.register("bootstrapLibraries").get()
val fabricLibrariesConfig = configurations.register("libraries") {
    extendsFrom(fabricBootstrapLibrariesConfig)
}.get()

val fabricMain by sourceSets.named("main") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, mixins, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, main, this, project, this.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameManagedLibraries)
        extendsFrom(fabricLibrariesConfig)
    }
}
val fabricLaunch by sourceSets.register("launch") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, main, this, project, this.implementationConfigurationName)

    spongeImpl.applyNamedDependencyOnOutput(project, this, fabricMain, project, fabricMain.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameManagedLibraries)
        extendsFrom(fabricLibrariesConfig)
    }
}

val fabricAppLaunch by sourceSets.register("applaunch") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch, this, project, this.implementationConfigurationName)

    spongeImpl.applyNamedDependencyOnOutput(project, this, fabricMain, project, fabricMain.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, fabricLaunch, project, fabricLaunch.implementationConfigurationName)


    configurations.named(implementationConfigurationName) {
        extendsFrom(gameManagedLibraries)
        extendsFrom(fabricBootstrapLibrariesConfig)
    }
}

val fabricMixins by sourceSets.register("mixins") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, main, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, mixins, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, fabricAppLaunch, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, fabricMain, this, project, this.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameManagedLibraries)
        extendsFrom(fabricLibrariesConfig)
    }
}

configurations.configureEach {
    exclude(group = "net.minecraft", module = "joined")
    if (name != "minecraft") { // awful terrible hack sssh (directly stolen from SpongeForge cause id probably never have known how to fix this if it wasn't for SpongeForge)
        exclude(group = "com.mojang", module = "minecraft")
    }
}

loom {
    accessWidenerPath.set(file("../src/main/resources/common.accesswidener"))

    mixin {
        useLegacyMixinAp.set(false)
    }

    mods {
        register("loofah") {
            sourceSet(fabricMixins)
            sourceSet(fabricAppLaunch)
            sourceSet(fabricLaunch)
            sourceSet(fabricMain)

            sourceSet(accessors, commonProject)
            sourceSet(mixins, commonProject)
            sourceSet(applaunch, commonProject)
            sourceSet(launch, commonProject)
            sourceSet(main, commonProject)
        }
    }

    runConfigs.configureEach {
        isIdeConfigGenerated = true
        var envPlugins = fabricLaunch.output.joinToString("&")
        testPluginsProject?.also {
            val plugins: FileCollection = it.sourceSets.getByName("main").output
            envPlugins = envPlugins.plus(";").plus(plugins.joinToString("&"))
        }
        environmentVariable("SPONGE_PLUGINS", envPlugins)
    }
}

tasks {
    withType(RunGameTask::class.java).configureEach {
        testPluginsProject?.also {
            dependsOn(it.sourceSets.getByName("main").output)
        }
    }
}

dependencies {
    val apiAdventureVersion: String by project
    val apiConfigurateVersion: String by project
    val apiGsonVersion: String by project
    val guavaVersion: String by project
    val apiPluginSpiVersion: String by project
    val jlineVersion: String by project

    //Loom minecraft config
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered() {
        officialMojangMappings { nameSyntheticMembers = true }
        // Temporarily disabled since parchment doesn't have 1.21.1 versions yet
        //parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentBuild")
    })
    gameManagedLibraries(modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")!!)

    // Mod dependencies
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    // API dependencies
    fabricBootstrapLibrariesConfig(apiLibs.pluginSpi) {
        exclude("org.apache.commons", "commons-lang3")
        exclude("org.apache.logging.log4j", "log4j-api")
        exclude("com.google.code.gson")
    }
    fabricBootstrapLibrariesConfig(platform(apiLibs.configurate.bom))
    fabricBootstrapLibrariesConfig(apiLibs.configurate.core)
    fabricBootstrapLibrariesConfig(apiLibs.configurate.hocon)
    fabricBootstrapLibrariesConfig(apiLibs.configurate.gson) {
        exclude("com.google.code.gson")
    }
    fabricBootstrapLibrariesConfig(apiLibs.configurate.yaml)
    fabricBootstrapLibrariesConfig(apiLibs.guice) {
        exclude("org.ow2.asm")
        exclude("com.google.guava")
    }
    fabricLibrariesConfig("org.spongepowered:spongeapi:$apiVersion")
    fabricLibrariesConfig(platform(apiLibs.adventure.bom))
    fabricLibrariesConfig(apiLibs.adventure.api)
    fabricLibrariesConfig(apiLibs.adventure.minimessage)
    fabricLibrariesConfig(apiLibs.adventure.textSerializer.gson) {
        exclude("com.google.code.gson")
    }
    fabricLibrariesConfig(apiLibs.adventure.textSerializer.plain)
    fabricLibrariesConfig(apiLibs.adventure.textSerializer.legacy)
    fabricLibrariesConfig(libs.adventure.serializerConfigurate4)
    fabricLibrariesConfig(apiLibs.math)
}

tasks {
    /* In case loofah specific access wideners are needed here's .... something to allow that
    //shitty hack shush the loader only supports one accesswidener file per mod
    register("mergeAccessWideners") {
        val generatedResourceDir = sourceSets.named("main").get().resources.srcDirs.first().resolve("generated")
        generatedResourceDir.mkdirs()
        val commonAccessWidenerDef = main.resources.srcDirs.first().resolve("common.accesswidener")
        commonAccessWidenerDef.createNewFile()
        val fabricAccessWidenerDef = fabricMain.resources.srcDirs.first().resolve("fabric.accesswidener")
        fabricAccessWidenerDef.createNewFile()

        generatedResourceDir
            .resolve("fabric.merged.accesswidener")
            .writeText(
                "accessWidener\tv1\tnamed\n" +
                        "\n# common.accesswidener\n" +
                        commonAccessWidenerDef.readText().substringAfter("\n") +
                        "\n# fabric.accesswidener\n" +
                        fabricAccessWidenerDef.readText().substringAfter("\n")
            )
    }*/

    withType<ProcessResources> {
        //dependsOn(named("mergeAccessWideners"))

        val props = mapOf(
            "javaVersion" to apiJavaTarget,
            "minecraftVersion" to minecraftVersion,
            "fabricLoaderVersion" to fabricLoaderVersion,
            "apiVersion" to apiVersion,
            "version" to project.version
        )
        props.forEach(inputs::property)

        filesMatching("fabric.mod.json") {
            expand(props)
        }

        filesMatching("META-INF/sponge_plugins.json") {
            expand(props)
        }
    }

    withType<AbstractRunTask> {
        // TODO(loofah): consider putting together the whole classpath on our own
        //  since the default one is a bit of a mess
        // Manually add mixins to the ide run classpaths since it won't get added by loom
        // since main doesn't (and can't) depend on mixins
        classpath(classpath, fabricMixins.output)
    }

    shadowJar {
        configurations = listOf(fabricLibrariesConfig)

        from(commonProject.sourceSets.main.map { it.output })
        from(commonProject.sourceSets.named("accessors").map {it.output })
        from(commonProject.sourceSets.named("mixins").map {it.output })
        from(commonProject.sourceSets.named("applaunch").map {it.output })
        from(commonProject.sourceSets.named("launch").map {it.output })
        from(commonProject.sourceSets.named("main").map {it.output })

        from(fabricMixins.output)
        from(fabricAppLaunch.output)
        from(fabricLaunch.output)
        from(fabricMain.output)
    }

    remapJar.get().enabled = false
    register("remapShadowJar", RemapJarTask::class) {
        build.get().finalizedBy(this)

        group = "shadow"
        archiveClassifier = "mod"

        inputFile.set(shadowJar.flatMap { it.archiveFile })
    }
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("HEADER.txt"))

    property("name", "Loofah")
    property("organization", "Nelind")
    property("url", "https://www.nelind.dk")
}
