plugins {
	kotlin("jvm") version "1.7.10"
	id("ru.capjack.publisher") version "1.0.0"
}

group = "ru.capjack.tool"
	
repositories {
	mavenCentral()
	mavenCapjack()
	mavenLocal()
}

kotlin {
	target.compilations.all { kotlinOptions.jvmTarget = "17" }
}

dependencies {
	implementation(kotlin("compiler-embeddable"))
	implementation("ru.capjack.tool:tool-lang:1.13.1")
	implementation("ru.capjack.tool:tool-logging:1.7.0")
	implementation("ru.capjack.tool:tool-utils:1.9.0")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.3")
	
	testImplementation(kotlin("test"))
	testImplementation("ch.qos.logback:logback-classic:1.2.11")
	testImplementation("ru.capjack.tool:tool-biser:1.6.0")
}