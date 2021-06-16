package ru.capjack.tool.biser.generator.langs.kotlin

import ru.capjack.tool.biser.generator.code.Code
import ru.capjack.tool.biser.generator.code.DependedCode
import ru.capjack.tool.biser.generator.model.*

class KotlinDecoderGenerator(
	private val model: Model,
	private val publicTypes: Set<Type>,
	private val encoderNaming: KotlinCoderNaming,
	private val typeNames: TypeVisitor<String, DependedCode>,
	private val readCalls: TypeVisitor<String, Unit>
) : TypeVisitor<Unit, Code>, EntityVisitor<Unit, Code> {
	
	private val dependencyDecoder = model.resolveEntityName("ru.capjack.tool.biser/Decoder")
	private val dependencyDecoders = model.resolveEntityName("ru.capjack.tool.biser/Decoders")
	private val dependencyUnknownIdDecoderException = model.resolveEntityName("ru.capjack.tool.biser/UnknownIdDecoderException")
	
	override fun visitPrimitiveType(type: PrimitiveType, data: Code) {
	}
	
	override fun visitEntityType(type: EntityType, data: Code) {
		data.addDependency(type.name)
		model.getEntity(type.name).accept(this, data)
	}
	
	override fun visitListType(type: ListType, data: Code) {
		writeDeclaration(type, data) {
			line(readCalls.visitListType(type, Unit))
		}
	}
	
	override fun visitMapType(type: MapType, data: Code) {
		writeDeclaration(type, data) {
			line(readCalls.visitMapType(type, Unit))
		}
	}
	
	override fun visitNullableType(type: NullableType, data: Code) {
		if (type.original == PrimitiveType.STRING) {
			return
		}
		writeDeclaration(type, data) {
			line(type.original.accept(readCalls))
		}
	}
	
	///
	
	override fun visitEnumEntity(entity: EnumEntity, data: Code) {
		val type = model.resolveEntityType(entity.name)
		val typeName = type.accept(typeNames, data)
		writeDeclaration(type, data) {
			identBracketsCurly("when (val id = readInt()) ") {
				entity.values.forEachIndexed { i, v ->
					line("$i -> $typeName.$v")
				}
				data.addDependency(dependencyUnknownIdDecoderException)
				line("else -> throw UnknownIdDecoderException(id, $typeName::class)")
			}
		}
	}
	
	override fun visitClassEntity(entity: ClassEntity, data: Code) {
		val type = model.resolveEntityType(entity.name)
		
		if (entity.children.isNotEmpty()) {
			
			if (!entity.abstract) {
				writeDeclaration(type, data, true).apply {
					writeClassEntityDecode(entity, data)
				}
			}
			
			writeDeclaration(type, data) {
				identBracketsCurly("when (val id = readInt()) ") {
					entity.allChildren.forEach { child ->
						val name = encoderNaming.resolveName(model.resolveEntityType(child.name))
						if (child is ClassEntity && !child.abstract && child.children.isNotEmpty())
							line("${child.id} -> ${name}_RAW()")
						else
							line("${child.id} -> $name()")
					}
					
					if (!entity.abstract) {
						val name = encoderNaming.resolveName(type)
						line("${entity.id} -> ${name}_RAW()")
					}
					
					val typeName = type.accept(typeNames, data)
					data.addDependency(dependencyUnknownIdDecoderException)
					line("else -> throw UnknownIdDecoderException(id, $typeName::class)")
				}
			}
		}
		else {
			writeDeclaration(type, data) {
				writeClassEntityDecode(entity, data)
			}
		}
	}
	
	override fun visitObjectEntity(entity: ObjectEntity, data: Code) {
		writeDeclaration(model.resolveEntityType(entity.name), data) {
			line(entity.name.internal.joinToString("."))
		}
	}
	
	///
	
	private fun Code.writeClassEntityDecode(entity: ClassEntity, code: Code) {
		identBracketsRound(entity.name.internal.joinToString(".")) {
			val last = entity.fields.size - 1
			entity.fields.forEachIndexed { i, field ->
				if (field.type is PrimitiveType) {
					code.addDependency(dependencyDecoders)
				}
				line(field.type.accept(readCalls) + (if (i == last) "" else ","))
			}
		}
	}
	
	private fun writeDeclaration(type: Type, code: Code, raw: Boolean = false): Code {
		code.addDependency(dependencyDecoder)
		
		var coderName = encoderNaming.resolveName(type)
		val typeName = type.accept(typeNames, code)
		
		if (raw) {
			coderName += "_RAW"
		}
		
		val block = code.identBracketsCurly((if (raw || !publicTypes.contains(type)) "private " else "") + "val $coderName: Decoder<$typeName> = ")
		
		code.line()
		
		return block
	}
	
	private inline fun writeDeclaration(type: Type, context: Code, code: Code.() -> Unit) {
		writeDeclaration(type, context).apply(code)
	}
}
