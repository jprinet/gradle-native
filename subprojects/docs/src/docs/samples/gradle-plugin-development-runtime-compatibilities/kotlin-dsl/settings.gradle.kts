pluginManagement {
	repositories {
		gradlePluginPortal()
		maven { url = uri("https://repo.nokeedev.net/release") }
	}
}

plugins {
	id("dev.gradleplugins.gradle-plugin-development") version("1.2")
}

rootProject.name = "gradle-plugin-development-runtime-compatibilities"
