package ru.capjack.tool.biser.generator.langs.kotlin

import ru.capjack.tool.biser.generator.CodeFile
import ru.capjack.tool.biser.generator.model.EntityName
import java.nio.file.Path

class KotlinCodeFile(private val name: EntityName) : CodeFile(name.nameSpace) {
	
	override fun save(path: Path) {
		val filePath = path.resolve(name.path.joinToString("/") + ".kt")
		super.save(filePath)
	}
	
	override fun writeHeader(builder: StringBuilder) {
		super.writeHeader(builder)
		
		name.external.also {
			if (it.isNotEmpty()) {
				builder.append("package ${it.joinToString(".")}\n\n")
			}
		}
		
		if (dependencies.isNotEmpty()) {
			dependencies
				.filter { name.external != it.name.external }
				.distinctBy { it.name.path }
				.sortedBy { it.name.path.joinToString(".") }
				.forEach {
					builder.append("import ${it.name.path.joinToString(".")}")
					if (it.aliases.isNotEmpty()) {
						builder.append(" as ${it.aliases.first()}")
					}
					builder.append("\n")
				}
			builder.append("\n")
		}
	}
}