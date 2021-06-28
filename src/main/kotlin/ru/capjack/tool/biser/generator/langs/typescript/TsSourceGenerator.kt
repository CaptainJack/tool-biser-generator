package ru.capjack.tool.biser.generator.langs.typescript

import ru.capjack.tool.biser.generator.Code
import ru.capjack.tool.biser.generator.DependedCode
import ru.capjack.tool.biser.generator.model.ClassEntity
import ru.capjack.tool.biser.generator.model.EntityName
import ru.capjack.tool.biser.generator.model.EntityVisitor
import ru.capjack.tool.biser.generator.model.EnumEntity
import ru.capjack.tool.biser.generator.model.ObjectEntity
import ru.capjack.tool.biser.generator.model.TypeVisitor

class TsSourceGenerator(
	private val typeNames: TypeVisitor<String, DependedCode>
) : EntityVisitor<Unit, Code> {
	
	override fun visitEnumEntity(entity: EnumEntity, data: Code) {
		data.identBracketsCurly("export enum ${entity.name.tsName} ") {
			entity.values.forEach { line("$it,") }
		}
	}
	
	override fun visitClassEntity(entity: ClassEntity, data: Code) {
		val parent = entity.parent?.let {
			data.addDependency(it.name)
			" extends " + it.name.tsName
		} ?: ""
		
		val abstract = if (entity.abstract) "abstract " else ""
		
		data.identBracketsCurly("export ${abstract}class ${entity.name.tsName}$parent ") {
			val protected = if (entity.abstract) "protected " else ""
			
			if (entity.fields.isEmpty()) {
				line("${protected}constructor() {")
			}
			else {
				line("${protected}constructor(")
				ident {
					entity.fields.forEach {
						line {
							if (entity.isFieldOwner(it.name)) append(if (it.readonly) "readonly " else "public ")
							append(it.name).append(": ").append(it.type.accept(typeNames, data)).append(",")
						}
					}
				}
				line(") {")
			}
			ident {
				entity.parent?.also { parent ->
					line {
						append("super(")
						parent.fields.joinTo(this) { it.name }
						append(")")
					}
				}
			}
			line("}")
		}
	}
	
	override fun visitObjectEntity(entity: ObjectEntity, data: Code) {
		val parent = entity.parent?.let {
			data.addDependency(it.name)
			" extends " + it.name.tsName
		} ?: ""
		
		data.identBracketsCurly("export class ${entity.name.tsName}$parent ") {
			line("static readonly INSTANCE = new ${entity.name.tsName}()")
			line()
			
			identBracketsCurly("private constructor() ") {
				line("super()")
			}
		}
	}
	
	private val EntityName.tsName: String
		get() = internal.joinToString("_")
}
