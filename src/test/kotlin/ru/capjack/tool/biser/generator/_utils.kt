package ru.capjack.tool.biser.generator

import java.nio.file.Path
import java.nio.file.Paths

private class Scope

fun readResource(name: String): String {
	return requireNotNull(Scope::class.java.classLoader.getResource(name)).readText()
}

fun getResourcePath(name: String): Path {
	return Paths.get(requireNotNull(Scope::class.java.classLoader.getResource(name)).toURI())
}