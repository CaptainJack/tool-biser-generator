package ru.capjack.tool.biser.generator

import ru.capjack.tool.biser.generator.model.EntityName
import ru.capjack.tool.biser.generator.model.NameSpace
import ru.capjack.tool.utils.collections.mutableKeyedSetOf
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Suppress("LeakingThis")
abstract class CodeFile(private val nameSpace: NameSpace) : DependedCode {
	protected val dependencies = mutableKeyedSetOf(CodeDependency::name)
	val header = Code(0, this)
	val body = Code(0, this)
	
	override fun addDependency(dependency: CodeDependency) {
		val d = dependencies[dependency.name]
		if (d == null) {
			dependencies.add(dependency)
		}
		else {
			d.aliases.addAll(dependency.aliases)
		}
	}
	
	override fun addDependency(name: String, vararg aliases: String) {
		addDependency(CodeDependency(nameSpace.resolveEntityName(name), *aliases))
	}
	
	open fun save(path: Path) {
		val builder = StringBuilder()
		writeHeader(builder)
		writeBody(builder)
		
		path.parent.createDirectories()
		path.writeText(builder.toString())
	}
	
	protected open fun writeHeader(builder: StringBuilder) {
		header.write(builder)
	}
	
	protected open fun writeBody(builder: StringBuilder) {
		body.write(builder)
	}
}


class CodeDependency(val name: EntityName) {
	val aliases = mutableSetOf<String>()
	
	constructor(entity: EntityName, vararg aliases: String) : this(entity) {
		this.aliases.addAll(aliases)
	}
}

interface CodeStatement {
	fun write(writer: StringBuilder)
}

class CodeLine(private val content: String) : CodeStatement {
	override fun write(writer: StringBuilder) {
		writer.append(content).append('\n')
	}
}

interface DependedCode {
	fun addDependency(dependency: CodeDependency)
	
	fun addDependency(name: String, vararg aliases: String)
	
	fun addDependency(name: EntityName) {
		addDependency(CodeDependency(name))
	}
	
	fun addDependency(name: EntityName, vararg aliases: String) {
		addDependency(CodeDependency(name, *aliases))
	}
}

class Code(private val ident: Int, private val file: CodeFile) : CodeStatement, DependedCode {
	private val statements = LinkedList<CodeStatement>()
	
	override fun addDependency(dependency: CodeDependency) {
		file.addDependency(dependency)
	}
	
	override fun addDependency(name: String, vararg aliases: String) {
		file.addDependency(name, *aliases)
	}
	
	fun line(v: String) {
		append(CodeLine("\t".repeat(ident) + v))
	}
	
	fun line() {
		line("")
	}
	
	inline fun line(v: StringBuilder.() -> Unit) {
		line(StringBuilder().apply(v).toString())
	}
	
	fun <T : CodeStatement> prepend(statement: T): T {
		statements.addFirst(statement)
		return statement
	}
	
	fun <T : CodeStatement> append(statement: T): T {
		statements.addLast(statement)
		return statement
	}
	
	fun ident(): Code {
		return append(Code(ident + 1, file))
	}
	
	inline fun ident(block: Code.() -> Unit) {
		ident().block()
	}
	
	fun identLine(v: String) {
		append(CodeLine("\t".repeat(ident + 1) + v))
	}
	
	fun identBracketsCurly(line: String): Code {
		line("$line{")
		val block = ident()
		line("}")
		return block
	}
	
	inline fun identBracketsCurly(line: String, block: Code.() -> Unit) {
		identBracketsCurly(line).block()
	}
	
	fun identBracketsRound(line: String): Code {
		line("$line(")
		val block = ident()
		line(")")
		return block
	}
	
	inline fun identBracketsRound(line: String, block: Code.() -> Unit) {
		identBracketsRound(line).block()
	}
	
	override fun write(writer: StringBuilder) {
		statements.forEach { it.write(writer) }
	}
}
