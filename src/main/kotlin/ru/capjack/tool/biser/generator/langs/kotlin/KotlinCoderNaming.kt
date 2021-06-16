package ru.capjack.tool.biser.generator.langs.kotlin

import ru.capjack.tool.biser.generator.model.EntityType
import ru.capjack.tool.biser.generator.model.ListType
import ru.capjack.tool.biser.generator.model.MapType
import ru.capjack.tool.biser.generator.model.NullableType
import ru.capjack.tool.biser.generator.model.PrimitiveType
import ru.capjack.tool.biser.generator.model.Type
import ru.capjack.tool.biser.generator.model.TypeVisitor

class KotlinCoderNaming(private val encoder: Boolean) {
	
	private val visitor = object : TypeVisitor<Unit, StringBuilder> {
		override fun visitPrimitiveType(type: PrimitiveType, data: StringBuilder) {
			data.append(type.name)
		}
		
		override fun visitEntityType(type: EntityType, data: StringBuilder) {
			data.append("entity_")
			type.name.internal.joinTo(data, "_")
		}
		
		override fun visitListType(type: ListType, data: StringBuilder) {
			data.append("list_")
			type.element.accept(this, data)
		}
		
		override fun visitMapType(type: MapType, data: StringBuilder) {
			data.append("map_")
			type.key.accept(this, data)
			data.append("_")
			type.value.accept(this, data)
		}
		
		override fun visitNullableType(type: NullableType, data: StringBuilder) {
			data.append("nullable_")
			type.original.accept(this, data)
		}
		
	}
	
	private val names = hashMapOf<Type, String>()
	
	fun resolveName(type: Type): String {
		return names.getOrPut(type) {
			if (type is PrimitiveType) {
				(if (encoder) "Encoders." else "Decoders.") + type.name
			}
			else {
				StringBuilder().also { type.accept(visitor, it) }.toString()
			}
		}
	}
}