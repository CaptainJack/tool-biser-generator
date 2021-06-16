package ru.capjack.tool.biser.generator.langs.kotlin

import ru.capjack.tool.biser.generator.code.Code
import ru.capjack.tool.biser.generator.code.DependedCode
import ru.capjack.tool.biser.generator.model.*

class KotlinEncoderGenerator(
	private val model: Model,
	private val publicTypes: Set<Type>,
	private val encoderNaming: KotlinCoderNaming,
	private val typeNames: TypeVisitor<String, DependedCode>,
	private val writeCalls: TypeVisitor<String, String>,
) : TypeVisitor<Unit, Code>, EntityVisitor<Unit, Code> {
	
	private val dependencyEncoder = model.resolveEntityName("ru.capjack.tool.biser/Encoder")
	private val dependencyEncoders = model.resolveEntityName("ru.capjack.tool.biser/Encoders")
	private val dependencyUnknownEntityEncoderException = model.resolveEntityName("ru.capjack.tool.biser/UnknownEntityEncoderException")
	
	override fun visitPrimitiveType(type: PrimitiveType, data: Code) {
	}
	
	override fun visitEntityType(type: EntityType, data: Code) {
		data.addDependency(type.name)
		model.getEntity(type.name).accept(this, data)
	}
	
	override fun visitListType(type: ListType, data: Code) {
		writeDeclaration(type, data) {
			line(writeCalls.visitListType(type, "it"))
		}
	}
	
	override fun visitMapType(type: MapType, data: Code) {
		writeDeclaration(type, data) {
			line(writeCalls.visitMapType(type, "it"))
		}
	}
	
	override fun visitNullableType(type: NullableType, data: Code) {
		if (type.original == PrimitiveType.STRING) {
			return
		}
		
		writeDeclaration(type, data) {
			identBracketsCurly("if (it == null) writeInt(0) else ") {
				line("writeInt(1)")
				line(type.original.accept(writeCalls, "it"))
			}
		}
	}
	
	///
	
	override fun visitEnumEntity(entity: EnumEntity, data: Code) {
		val type = model.resolveEntityType(entity.name)
		val typeName = type.accept(typeNames, data)
		writeDeclaration(type, data) {
			line("writeInt(when (it) {")
			ident {
				entity.values.forEachIndexed { i, v ->
					line("$typeName.$v -> $i")
				}
			}
			line("})")
		}
	}
	
	override fun visitClassEntity(entity: ClassEntity, data: Code) {
		writeDeclaration(model.resolveEntityType(entity.name), data) {
			if (entity.children.isNotEmpty()) {
				
				identBracketsCurly("when (it) ") {
					entity.children.forEach { child ->
						val childType = model.resolveEntityType(child.name)
						val childTypeName = childType.accept(typeNames, data)
						
						identBracketsCurly("is $childTypeName -> ") {
							if (child is ObjectEntity || (child is ClassEntity && child.children.isEmpty())) {
								line("writeInt(${child.id})")
							}
							line("${encoderNaming.resolveName(childType)}(it)")
						}
					}
					
					if (entity.abstract) {
						if (!entity.sealed) {
							data.addDependency(dependencyUnknownEntityEncoderException)
							line("else -> throw UnknownEntityEncoderException(it)")
						}
					}
					else {
						identBracketsCurly("else -> ") {
							writeClassEntityFields(entity, data)
						}
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
			line("writeInt(${entity.id})")
		}
		
		entity.fields.forEach { field ->
			if (field.type is PrimitiveType) {
				code.addDependency(dependencyEncoders)
			}
			line(field.type.accept(writeCalls, "it.${field.name}"))
		}
	}
	
	
	private fun writeDeclaration(type: Type, code: Code): Code {
		code.addDependency(dependencyEncoder)
		
		val coderName = encoderNaming.resolveName(type)
		val typeName = type.accept(typeNames, code)
		
		val block = code.identBracketsCurly((if (publicTypes.contains(type)) "" else "private ") + "val $coderName: Encoder<$typeName> = ")
		
		code.line()
		
		return block
	}
	
	private inline fun writeDeclaration(type: Type, context: Code, code: Code.() -> Unit) {
		writeDeclaration(type, context).apply(code)
	}
}


