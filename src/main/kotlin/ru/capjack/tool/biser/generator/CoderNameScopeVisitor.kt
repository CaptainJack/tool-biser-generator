package ru.capjack.tool.biser.generator

interface CoderNameScopeVisitor {
	fun visitPrimitiveScope(name: String, depended: DependedCode): String
	
	fun visitGeneratedScope(name: String, depended: DependedCode): String
}