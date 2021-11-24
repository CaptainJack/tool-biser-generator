plugins {
	kotlin("jvm") version "1.5.31"
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
	implementation("ru.capjack.tool:tool-utils:1.7.0")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")
	
	testImplementation(kotlin("test"))
	testImplementation("ch.qos.logback:logback-classic:1.2.7")
	testImplementation("ru.capjack.tool:tool-biser:1.1.0")
}