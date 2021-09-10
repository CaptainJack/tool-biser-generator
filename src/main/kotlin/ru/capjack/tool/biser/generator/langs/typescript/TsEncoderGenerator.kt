package ru.capjack.tool.biser.generator.langs.typescript

import ru.capjack.tool.biser.generator.Code
import ru.capjack.tool.biser.generator.CodeDependency
import ru.capjack.tool.biser.generator.DependedCode
import ru.capjack.tool.biser.generator.langs.DefaultWriteCallVisitor
import ru.capjack.tool.biser.generator.model.*

class TsEncoderGenerator(
	private val model: Model,
	private val publicTypes: Set<Type>,
	private val encoderNames: TypeVisitor<String, DependedCode>,
	private val typeNames: TypeVisitor<String, DependedCode>
) : TypeVisitor<Unit, Code>, EntityVisitor<Unit, Code> {
	
	private val writeCalls = DefaultWriteCallVisitor(encoderNames)
	
	private val dependencyEncoder = model.resolveEntityName("ru.capjack.tool.biser/Encoder")
	private val dependencyUnknownEntityEncoderException = CodeDependency(model.resolveEntityName("ru.capjack.tool.biser/_exceptions"), "UnknownEntityEncoderException")
	
	override fun visitPrimitiveType(type: PrimitiveType, data: Code) {
	}
	
	override fun visitEntityType(type: EntityType, data: Code) {
		data.addDependency(type.name)
		model.getEntity(type.name).accept(this, data)
	}
	
	override fun visitListType(type: ListType, data: Code) {
		writeDeclaration(type, data) {
			line(writeCalls.visit(type, data, "v"))
		}
	}
	
	override fun visitMapType(type: MapType, data: Code) {
		writeDeclaration(type, data) {
			line(writeCalls.visit(type, data, "v"))
		}
	}
	
	override fun visitNullableType(type: NullableType, data: Code) {
		if (type.original == PrimitiveType.STRING) {
			return
		}
		
		writeDeclaration(type, data) {
			identBracketsCurly("if (v === null) w.writeBoolean(false); else ") {
				line("w.writeBoolean(true)")
				line("w." + writeCalls.visit(type.original, data, "v"))
			}
		}
	}
	
	///
	
	override fun visitEnumEntity(entity: EnumEntity, data: Code) {
		val type = model.resolveEntityType(entity.name)
		val typeName = type.accept(typeNames, data)
		writeDeclaration(type, data) {
			identBracketsCurly("switch (v) ") {
				entity.values.forEachIndexed { i, v ->
					line("case $typeName.$v: w.writeInt($i); break")
				}
			}
		}
	}
	
	override fun visitClassEntity(entity: ClassEntity, data: Code) {
		writeDeclaration(model.resolveEntityType(entity.name), data) {
			if (entity.children.isNotEmpty()) {
				
				entity.children.forEachIndexed { i, child ->
					val childType = model.resolveEntityType(child.name)
					val childTypeName = childType.accept(typeNames, data)
					
					
					identBracketsCurly("${if (i != 0) "else " else ""}if (v instanceof $childTypeName)") {
						if (child is ObjectEntity || (child is ClassEntity && child.children.isEmpty())) {
							line("w.writeInt(${child.id})")
						}
						line("${childType.accept(encoderNames, data)}(w, v)")
					}
				}
				
				if (entity.abstract) {
					if (!entity.sealed) {
						data.addDependency(dependencyUnknownEntityEncoderException)
						line("else -> throw new UnknownEntityEncoderException(it)")
					}
				}
				else {
					identBracketsCurly("else -> ") {
						writeClassEntityFields(entity, data)
					}
				}
				
			}
			else {
				writeClassEntityFields(entity, data)
			}
		}
	}
	
	override fun visitObjectEntity(entity: ObjectEntity, data: Code) {
		writeDeclaration(model.resolveEntityType(entity.name), data) {}
	}
	
	///
	
	private fun Code.writeClassEntityFields(entity: ClassEntity, code: Code) {
		if (entity.children.isNotEmpty()) {
			line("w.writeInt(${entity.id})")
		}
		
		entity.fields.forEach { field ->
			line("w." + writeCalls.visit(field.type, code, "v.${field.name}"))
		}
	}
	
	
	private fun writeDeclaration(type: Type, code: Code): Code {
		code.addDependency(dependencyEncoder)
		
		val coderName = type.accept(encoderNames, code)
		val typeName = type.accept(typeNames, code)
		
		val block = code.identBracketsCurly((if (publicTypes.contains(type)) "export " else "") + "const $coderName: Encoder<$typeName> = (w, v) => ")
		
		code.line()
		
		return block
	}
	
	private inline fun writeDeclaration(type: Type, context: Code, code: Code.() -> Unit) {
		writeDeclaration(type, context).apply(code)
	}
}


