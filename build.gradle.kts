plugins {
	kotlin("jvm") version "1.5.10"
	id("ru.capjack.publisher") version "1.0.0"
}

group = "ru.capjack.tool"
	
repositories {
	mavenCentral()
	mavenCapjack()
	mavenLocal()
}

kotlin {
	target.compilations.all { kotlinOptions.jvmTarget = "11" }
}

dependencies {
	implementation(kotlin("compiler-embeddable"))
	implementation("ru.capjack.tool:tool-logging:1.5.0")
	implementation("ru.capjack.tool:tool-lang:1.11.1")
	implementation("ru.capjack.tool:tool-utils:1.6.1")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.3")
	
	testImplementation(kotlin("test"))
	testImplementation("ch.qos.logback:logback-classic:1.2.3")
	testImplementation("ru.capjack.tool:tool-biser:1.0.0")
}