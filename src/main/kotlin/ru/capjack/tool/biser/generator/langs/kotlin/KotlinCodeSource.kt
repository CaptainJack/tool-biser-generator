package ru.capjack.tool.biser.generator.langs.kotlin

import ru.capjack.tool.biser.generator.CodeSource
import ru.capjack.tool.biser.generator.EntityCodeFile
import ru.capjack.tool.biser.generator.model.EntityName
import java.nio.file.Path

class KotlinCodeSource(private val rootDir: Path) : CodeSource() {
	override fun defineEntityFilePath(name: EntityName): Path {
		return rootDir.resolve(name.path.joinToString("/") + ".kt")
	}
	
	override fun writeEntityFileHeader(path: Path, file: EntityCodeFile, builder: StringBuilder) {
		super.writeEntityFileHeader(path, file, builder)
		
		file.name.external.also {
			if (it.isNotEmpty()) {
				builder.append("package ${it.joinToString(".")}\n\n")
			}
		}
		
		if (file.dependencies.isNotEmpty()) {
			file.dependencies
				.filter { file.name.external != it.name.external }
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