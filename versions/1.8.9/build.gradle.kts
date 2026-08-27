plugins {
	id("net.fabricmc.fabric-loom-remap")
	id("ploceus")
}

val minecraftVersion = "1.8.9"
val featherBuild = "1"
val lwjglVersion = "3.4.2"
val legacyLwjgl3 = "1.4.3"
val osl = "0.20.3"
base.archivesName = "DolphinClient"
group = project.property("maven_group")!!
version = "${project.property("version")}+$minecraftVersion"

repositories {
	exclusiveContent {
		forRepository { mavenCentral() }
		filter { includeGroup("org.lwjgl") }
	}
}

loom {
	accessWidenerPath.set(file("src/main/resources/axolotlclient.classtweaker"))

	mods {
		create("dolphinclient") {
			sourceSet("main")
		}
		create("dolphinclient-test") {
			sourceSet("test")
		}
	}
	uncompressNestedJars = true
}

ploceus {
	setIntermediaryGeneration(2)
}

dependencies {
	minecraft("com.mojang:minecraft:$minecraftVersion")
	mappings(ploceus.featherMappings(featherBuild))

	modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader")}")

	include(modImplementation("io.github.axolotlclient:AxolotlClient-config:${project.property("config")}+$minecraftVersion")!!)
	modImplementation(include("io.github.axolotlclient:AxolotlClient-config-rounded:${project.property("config")}+$minecraftVersion")!!)

	ploceus.dependOsl(osl)

	modImplementation("com.terraformersmc:modmenu:0.4.0+mc1.8.9")

	api(include(project(path = ":common", configuration = "shadow"))!!)

	compileOnly("org.lwjgl:lwjgl-sdl:$lwjglVersion")

	modImplementation("io.github.moehreag:legacy-lwjgl3:$legacyLwjgl3")

	include(implementation("org.lwjgl:lwjgl-tinyfd:$lwjglVersion")!!)
	include(runtimeOnly("org.lwjgl:lwjgl-tinyfd:$lwjglVersion:natives-linux")!!)
	include(runtimeOnly("org.lwjgl:lwjgl-tinyfd:$lwjglVersion:natives-windows")!!)
	include(runtimeOnly("org.lwjgl:lwjgl-tinyfd:$lwjglVersion:natives-macos")!!)
	include(runtimeOnly("org.lwjgl:lwjgl-tinyfd:$lwjglVersion:natives-windows-arm64")!!)
	include(runtimeOnly("org.lwjgl:lwjgl-tinyfd:$lwjglVersion:natives-macos-arm64")!!)

	api("net.hypixel:mod-api:1.0.2")
	include(modImplementation("io.github.moehreag.hypixel:mod-api-fabric:1.0.2+build.1+mc1.8.9")!!)
	include(implementation("com.mojang:brigadier:1.0.18")!!)
}

configurations.configureEach {
	exclude("org.lwjgl.lwjgl")
	resolutionStrategy {
		dependencySubstitution {
			substitute(module("io.netty:netty-all:4.0.23.Final")).using(module("io.netty:netty-all:4.0.56.Final"))
		}
		force("io.netty:netty-all:4.0.56.Final")
	}
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.runClient {
	classpath(sourceSets.getByName("test").runtimeClasspath)
	jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:+IgnoreUnrecognizedVMOptions")
	environment("LEGACY_LWJGL3_USE_SDL", "true")
}

tasks.withType(JavaCompile::class).configureEach {
	options.encoding = "UTF-8"

	if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_18)) {
		options.release = 17
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

// Configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}

	repositories {
		maven {
			name = "owlMaven"
			val repository = if (project.version.toString().contains("beta") || project.version.toString()
					.contains("alpha")
			) "snapshots" else "releases"
			url = uri("https://maven.axolotlclient.com/$repository")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
}

publishMods {
	file.set(tasks.remapJar.flatMap { it.archiveFile })
	additionalFiles.from(tasks.remapSourcesJar.flatMap { it.archiveFile })
	changelog.set(rootProject.layout.buildDirectory.file("changelog").map { it.asFile.readText() })
	type.set(STABLE)
	modLoaders.add("ornithe")


	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("p2rxzX0q")
		minecraftVersions.set(listOf(minecraftVersion))
		requires { slug = "fabric-api" }
		embeds { slug = "axolotlclient-rendering" }
	}

	// CurseForge doesn't support Ornithe
}

tasks.getByName("publishModrinth") {
	dependsOn(rootProject.tasks.getByName("generateVersionChangelog"))
}
