plugins {
	id("dev.gradleplugins.java-gradle-plugin") version("1.6.7")
	id("dev.gradleplugins.gradle-plugin-functional-test") version("1.6.7")
	id("groovy-base") // for Spock testing
}

gradlePlugin {
	plugins {
		create("helloWorld") {
			id = "com.example.hello"
			implementationClass = "com.example.BasicPlugin"
		}
	}
}

repositories {
	mavenCentral()
}

functionalTest {
	testingStrategies.set(listOf(strategies.coverageForMinimumVersion, strategies.coverageForLatestGlobalAvailableVersion, strategies.coverageForLatestNightlyVersion))
	dependencies {
		implementation(platform("org.spockframework:spock-bom:2.0-groovy-3.0"))
		implementation("org.spockframework:spock-core")
		implementation(gradleTestKit())
	}
	testTasks.configureEach { useJUnitPlatform() }
}
