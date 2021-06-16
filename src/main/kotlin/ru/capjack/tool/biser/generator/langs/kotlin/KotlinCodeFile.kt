package ru.capjack.tool.biser.generator.langs.kotlin

import ru.capjack.tool.biser.generator.code.CodeFile
import ru.capjack.tool.biser.generator.model.EntityName
import java.nio.file.Path

class KotlinCodeFile(private val entityName: EntityName) : CodeFile() {
	
	override fun save(path: Path) {
		val filePath = path.resolve(entityName.path.joinToString("/") + ".kt")
		super.save(filePath)
	}
	
	override fun writeHeader(builder: StringBuilder) {
		entityName.external.also {
			if (it.isNotEmpty()) {
				builder.append("package ${it.joinToString(".")}\n\n")
			}
		}
		
		if (dependencies.isNotEmpty()) {
			dependencies
				.filter { entityName.external != it.entity.external }
				.distinctBy { it.entity.path }
				.sortedBy { it.entity.path.joinToString(".") }
				.forEach {
					builder.append("import ${it.entity.path.joinToString(".")}")
					if (it.aliases.isNotEmpty()) {
						builder.append(" as ${it.aliases.first()}")
					}
					builder.append("\n")
				}
			builder.append("\n")
		}
	}
}