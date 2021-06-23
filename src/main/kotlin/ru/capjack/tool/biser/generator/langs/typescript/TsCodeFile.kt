package ru.capjack.tool.biser.generator.langs.typescript

import ru.capjack.tool.biser.generator.CodeFile
import ru.capjack.tool.biser.generator.model.EntityName
import java.nio.file.Path

class TsCodeFile(private val name: EntityName) : CodeFile(name.nameSpace) {
	
	override fun save(path: Path) {
		
		val filePath = path.resolve((name.external + name.internal.joinToString("_")).joinToString("/") + ".ts")
		super.save(filePath)
	}
	
	override fun writeHeader(builder: StringBuilder) {
		super.writeHeader(builder)
		if (dependencies.isNotEmpty()) {
			dependencies
				.filter { it.name != name }
				.sortedBy { it.name.full.joinToString(".") }
				.forEach {
					val aliases = it.aliases.toMutableList()
					if (aliases.isEmpty()) {
						aliases.add(it.name.internal.joinToString("_"))
					}
					aliases.sort()
					
					val itName = it.name.internal.joinToString("_")
					
					val path =
						if (name.external == it.name.external) {
							"./$itName"
						}
						else {
							val external = it.name.external.toMutableList()
							val relative = mutableListOf<String>()
							var matched = false
							while (external.isNotEmpty()) {
								relative.add(external.removeLast())
								if (external == name.external) {
									matched = true
									break
								}
							}
							if (matched) {
								relative.reverse()
								"./" + relative.joinToString("/") + "/" + itName
							}
							else {
								external.clear()
								external.addAll(it.name.external)
								relative.clear()
								relative.addAll(name.external)
								
								while (external.isNotEmpty() && relative.isNotEmpty()) {
									val a = external.first()
									val b = relative.first()
									if (a == b) {
										external.removeFirst()
										relative.removeFirst()
									}
									else {
										break
									}
								}
								
								relative.joinToString("/") { ".." } + (if (external.isEmpty()) "" else external.joinToString("/", "/")) + "/" + itName
							}
						}
					
					builder.append("import {${aliases.joinToString(", ")}} from '$path'")
					builder.append("\n")
				}
			builder.append("\n")
		}
	}
}