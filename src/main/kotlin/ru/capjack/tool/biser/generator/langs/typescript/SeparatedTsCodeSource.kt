package ru.capjack.tool.biser.generator.langs.typescript

import ru.capjack.tool.biser.generator.CodeSource
import ru.capjack.tool.biser.generator.EntityCodeFile
import ru.capjack.tool.biser.generator.model.EntityName
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

class SeparatedTsCodeSource(
	rootDir: Path,
	private val genPrefix: String,
	genPath: String,
	libPath: String,
	cutGenPrefix: String? = null,
	cutLibPrefix: String? = null
) : CodeSource() {
	
	val genPath = rootDir.resolve(genPath).toAbsolutePath()
	private val libPath = rootDir.resolve(libPath).toAbsolutePath()
	
	private val cutGenPrefix = cutGenPrefix?.replace('.', '/')
	private val cutLibPrefix = cutLibPrefix?.replace('.', '/')
	
	override fun defineEntityFilePath(name: EntityName): Path {
		var path = (name.external + name.internal.joinToString("_")).joinToString("/") + ".ts"
		
		if (name.path.joinToString(".").startsWith(genPrefix)) {
			if (cutGenPrefix != null) {
				path = path.substringAfter(cutGenPrefix).trimStart('/')
			}
			return genPath.resolve(path)
		}
		if (cutLibPrefix != null) {
			path = path.substringAfter(cutLibPrefix).trimStart('/')
		}
		return libPath.resolve(path)
	}
	
	override fun writeEntityFileHeader(path: Path, file: EntityCodeFile, builder: StringBuilder) {
		super.writeEntityFileHeader(path, file, builder)
		
		if (file.dependencies.isNotEmpty()) {
			
			file.dependencies
				.filter { it.name != file.name }
				.sortedBy { it.name.full.joinToString(".") }
				.forEach {
					val aliases = it.aliases.toMutableList()
					val dependencyName = it.name
					if (aliases.isEmpty()) {
						aliases.add(dependencyName.internal.joinToString("_"))
					}
					aliases.sort()
					
					val dir = path.parent
					val dependencyPath = defineEntityFilePath(it.name)
					val dependencyDir = dependencyPath.parent
					var relativePath = dir.relativize(dependencyDir).toString()
					
					if (relativePath.isEmpty()) {
						relativePath = "./${dependencyPath.nameWithoutExtension}"
					}
					else if (relativePath.first() != '.') {
						relativePath = "./$relativePath/${dependencyPath.nameWithoutExtension}"
					}
					else if (relativePath == "..") {
						relativePath = "../${dependencyPath.nameWithoutExtension}"
					}
					else {
						relativePath = "$relativePath/${dependencyPath.nameWithoutExtension}"
					}
					
					builder.append("import {${aliases.joinToString(", ")}} from '$relativePath'").append('\n')
				}
			builder.append("\n")
		}
	}
}


/*

val dependencyInternalName = dependencyName.internal.joinToString("_")
		
		if (targetName.external == dependencyName.external) {
			return "./$dependencyInternalName"
		}
		
		val external = dependencyName.external.toMutableList()
		val relative = mutableListOf<String>()
		var matched = false
		while (external.isNotEmpty()) {
			relative.add(external.removeLast())
			if (external == targetName.external) {
				matched = true
				break
			}
		}
		
		if (matched) {
			relative.reverse()
			return "./" + relative.joinToString("/") + "/" + dependencyInternalName
		}
		
		external.clear()
		external.addAll(dependencyName.external)
		relative.clear()
		relative.addAll(targetName.external)
		
		while (external.isNotEmpty() && relative.isNotEmpty()) {
			val a = external.first()
			val b = relative.first()
			if (a == b) {
				external.removeFirst()
				relative.removeFirst()
			} else {
				break
			}
		}
		
		return relative.joinToString("/") { ".." } + (if (external.isEmpty()) "" else external.joinToString("/", "/")) + "/" + dependencyInternalName

class TsCodeFile(private val name: EntityName, private val filePathResolver: TsFilePathResolver) : CodeFile(name.nameSpace) {
	
	override fun save(path: Path): Path {
		val filePath = filePathResolver.resolveSavePath(path, name)
		return super.save(filePath)
	}
	
	override fun writeHeader(builder: StringBuilder) {
		super.writeHeader(builder)
		if (dependencies.isNotEmpty()) {
			val targetName = name
			
			dependencies
				.filter { it.name != targetName }
				.sortedBy { it.name.full.joinToString(".") }
				.forEach {
					val aliases = it.aliases.toMutableList()
					val dependencyName = it.name
					if (aliases.isEmpty()) {
						aliases.add(dependencyName.internal.joinToString("_"))
					}
					aliases.sort()
					
					val path = filePathResolver.resolveDependencyPath(targetName, dependencyName)
					
					builder.append("import {${aliases.joinToString(", ")}} from '$path'")
					builder.append("\n")
				}
			builder.append("\n")
		}
	}
}*/
