import me.soostrator.cti.plugin.ResolveDirectoryTask
import me.soostrator.cti.plugin.ResolveJarTask
import me.soostrator.cti.plugin.ResolverExtension
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.spongepowered.asm.gradle.plugins.MixinExtension

buildscript {
    repositories {
        mavenCentral()

        maven("https://maven.minecraftforge.net/")
        maven("https://files.minecraftforge.net/maven/")
    }

    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:4.+")
        classpath("org.spongepowered:mixingradle:0.7.+")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
        classpath(files("libs${File.separator}CtiPlugin-1.0.jar"))
    }
}

plugins {
    java
    kotlin("jvm") version "1.7.20"
}

apply {
    plugin("me.soostrator.resolver")
    plugin("net.minecraftforge.gradle")
    plugin("org.spongepowered.mixin")
    plugin("kotlin")
    plugin("idea")
}

version = "1.1.0"
group = "com.paragon"

repositories {
    mavenCentral()

    maven("https://files.minecraftforge.net/maven/")
    maven("https://jitpack.io")
    maven("https://impactdevelopment.github.io/maven/")
}

configure<UserDevExtension> {
    mappings(mapOf("channel" to "snapshot", "version" to "20171003-1.12"))

    runs {
        create("client") {
            workingDirectory = project.file("run").path

            properties(
                mapOf(
                    "forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP",
                    "forge.logging.console.level" to "debug",
                    "fml.coreMods.load" to "com.paragon.MixinLoader"
                )
            )
        }
    }
}

configurations.create("libraries")

dependencies {
    implementation("com.github.SooStrator1136:CompileTimeInheritance:master-SNAPSHOT")

    "minecraft"("net.minecraftforge:forge:1.12.2-14.23.5.2860")

    "libraries"("org.spongepowered:mixin:0.8") {
        exclude("module", "guava")
        exclude("module", "gson")
        exclude("module", "commons-io")
    }

    annotationProcessor("org.spongepowered:mixin:0.8:processor") {
        exclude("module", "gson")
    }

    "libraries"("org.json:json:20220924")
    "libraries"("com.github.Litarvan:OpenAuth:1.1.2")
    "libraries"("club.minnced:java-discord-rpc:2.0.1")
    "libraries"("com.github.therealbush:translator:1.0.2")
    "libraries"("com.github.Wolfsurge:animationsystem:1.1")
    "libraries"("com.github.cabaletta:baritone:1.2.14")

    "libraries"("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20") {
        exclude("module", "kotlin-stdlib-common")
        exclude("module", "annotations")
    }

    "libraries"("org.jetbrains.kotlin:kotlin-reflect:1.7.20") {
        exclude("module", "kotlin-stdlib")
    }

    "libraries"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4") {
        exclude("module", "kotlin-stdlib-jdk8")
        exclude("module", "kotlin-stdlib-common")
    }

    implementation(configurations.getByName("libraries"))
}

configure<MixinExtension> {
    defaultObfuscationEnv = "searge"
    add(project.sourceSets["main"], "mixins.paragon.refmap.json")
    config("mixins.paragon.json")
}

tasks.processResources {
    from(project.sourceSets["main"].resources.srcDirs) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        include("mcmod.info")
        expand(
            mapOf(
                "version" to version,
                "mcversion" to "1.12.2"
            )
        )
    }
}

tasks.jar {
    manifest.attributes(
        "Manifest-Version" to "1.0",
        "MixinConfigs" to "mixins.paragon.json",
        "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
        "FMLCorePluginContainsFMLMod" to "true",
        "FMLCorePlugin" to "com.paragon.MixinLoader",
        "ForceLoadAsMod" to "true"
    )

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(
        configurations.getByName("libraries").map {
            if (it.isDirectory) it else zipTree(it)
        }
    )
}

sourceSets["main"].java.srcDir("src/main/kotlin")

tasks.register<Copy>("prepareAssets") {
    group = "paragon"

    from(project.file("src/main/resources"))
    into(project.file("build/classes/kotlin/main"))
}

tasks.register<Jar>("buildApi") {
    group = "paragon"

    archiveClassifier.set("api")
    from(project.sourceSets["main"].output)
}

tasks.getByName("classes").dependsOn("prepareAssets")

configure<ResolverExtension> {
    isUseClassForName = false
    isResolveFoundJars = false
}

tasks.register<ResolveDirectoryTask>("resolveClasses") {
    group = "paragon"

    inputDirectory = sourceSets["main"].output.classesDirs.first { it.parentFile.name == "kotlin" }.absoluteFile
}

tasks.getByName("compileKotlin").finalizedBy("resolveClasses")

tasks.register<ResolveJarTask>("resolveJar") {
    group = "paragon"

    val jarName = "${rootProject.name}-${version}"

    inputJar = File("build${File.separator}libs${File.separator}${jarName}.jar")
    outputJar = File("build${File.separator}libs${File.separator}${jarName}-resolved.jar")
    println(rootProject.name)
}

tasks.getByName("build").finalizedBy("resolveJar")