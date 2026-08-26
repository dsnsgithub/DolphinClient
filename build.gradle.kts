import java.nio.file.FileSystems
import kotlin.io.path.*

plugins {
	id("io.freefair.lombok") version "9.2.0" apply false
	id("me.modmuss50.mod-publish-plugin") version "2.0.0" apply false
	id("com.gradleup.shadow") version "9.3.1" apply false
	id("dev.yumi.gradle.licenser") version "2.0.+"
	id("net.fabricmc.fabric-loom-remap") version "1.17.+" apply false
	id("net.fabricmc.fabric-loom") version "1.17.+" apply false
	id("ploceus") version "1.17.+" apply false
}

version = "${project.version}"
group = "io.github.axolotlclient"

repositories {
	maven {
		url = uri("https://maven.axolotlclient.com/releases")
	}
	mavenCentral()
}

allprojects {
	repositories {
		maven("https://maven.terraformersmc.com/releases")
		maven("https://maven.fabricmc.net")
		maven("https://maven.quiltmc.org/repository/release")
		maven("https://maven.axolotlclient.com/releases")
		maven("https://maven.axolotlclient.com/snapshots")
		maven("https://maven.parchmentmc.org")
		maven("https://libraries.minecraft.net/")
		maven("https://repo.hypixel.net/repository/Hypixel/") {
			content {
				includeGroup("net.hypixel")
			}
		}
		exclusiveContent {
			forRepository {
				maven("https://api.modrinth.com/maven")
			}
			filter {
				includeGroup("maven.modrinth")
			}
		}
		mavenLocal()
		mavenCentral()
		maven("https://central.sonatype.com/repository/maven-snapshots")
	}

	tasks.withType<AbstractTestTask>().configureEach {
		failOnNoDiscoveredTests = false
	}
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "maven-publish")
	apply(plugin = "io.freefair.lombok")
	if (project.name != "common") apply(plugin = "me.modmuss50.mod-publish-plugin")
	apply(plugin = "dev.yumi.gradle.licenser")

	extensions.getByType(JavaPluginExtension::class).withSourcesJar()

	tasks.getByName("jar", Jar::class) {
		filesMatching("LICENSE") {
			rename("^(LICENSE.*?)(\\..*)?$", "$1_${archiveBaseName}$2")
		}
	}

	license {
		rule(rootProject.file("HEADER"))
		include("**/*.java")
		exclude("**/oldanimations/**")
	}

	tasks.register("collectBuilds") {
		dependsOn(tasks.getByName("build"))
		if (project.name == "common") {
			enabled = false
		}
		val projectVersion = project.version
		val buildDir = project.layout.buildDirectory.dir("libs").get()
		val rootProjectDir = rootProject.projectDir
		val synchronizer = Unit
		actions.addLast {
			val outDir = rootProjectDir.resolve("builds").toPath()
			outDir.createDirectories()
			val archiveDir = outDir.resolve("archive")
			outDir.listDirectoryEntries().forEach { old ->
				if (!old.isRegularFile()) {
					return@forEach
				}
				val oldName = old.fileName.toString()
				val oldVer = oldName.substringBefore("+")
				val mcVer = oldName.substring(oldName.indexOf("+") + 1, oldName.length - 4).removeSuffix("-sources")
				if (!projectVersion.toString().contains(mcVer)) {
					return@forEach
				}
				// check if it's the current version, if it is we don't archive it
				if (projectVersion.toString().contains(oldVer.substring(oldVer.indexOf("-") + 1))) {
					return@forEach
				}
				archiveDir.createDirectories()
				val versionArchive = archiveDir.resolve("$oldVer.zip")
				synchronized(synchronizer) {
					if (old.exists()) {
						(if (versionArchive.notExists()) {
							FileSystems.newFileSystem(versionArchive, mapOf("create" to "true"))
						} else {
							FileSystems.newFileSystem(versionArchive)
						}).use {
							old.moveTo(it.getPath(oldName))
						}
					}
				}
			}
			buildDir.asFileTree.files.forEach { file ->
				if (file.name.contains(projectVersion.toString())) {
					file.toPath().copyTo(outDir.resolve(file.name.toString()), overwrite = true)
				}
			}
		}
	}

	tasks.register("publishUnstable") {
		if (project.version.toString().contains("beta") || project.version.toString()
				.contains("alpha")
		) {
			dependsOn("publish")
		} else {
			actions.add {
				println("Project doesn't use an -alpha or -beta version, not publishing unstable.")
			}
		}
	}
}

tasks.register("generateVersionChangelog") {
	actions.addLast {
		val changelogText = project.layout.projectDirectory.file("CHANGELOG.md").asFile.readText()
		val regexVersion =
			((project.version) as String).split("+")[0].replace("\\.".toRegex(), "\\.").replace("\\+".toRegex(), "+")
		val changelogRegex = "###? ${regexVersion}\\n\\n(( *- .+\\n)+)".toRegex()
		val matcher = changelogRegex.find(changelogText)

		val out = project.layout.buildDirectory.file("changelog").get().asFile.toPath()
		if (matcher != null) {
			var changelogContent = matcher.groups[1]?.value!!

			val changelogLines = changelogText.split("\n")
			val linkRefRegex = "^\\[([A-z0-9 _\\-/+.]+)]: ".toRegex()
			for (line in changelogLines.reversed()) {
				if ((linkRefRegex.matches(line)))
					changelogContent += "\n" + line
				else break
			}

			out.writeText(changelogContent)
		} else {
			out.writeText("")
		}
	}
}

