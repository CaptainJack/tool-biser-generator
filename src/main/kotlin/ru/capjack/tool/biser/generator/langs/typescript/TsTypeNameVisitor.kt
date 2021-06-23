package ru.capjack.tool.biser.generator.langs.typescript

import ru.capjack.tool.biser.generator.DependedCode
import ru.capjack.tool.biser.generator.model.EntityType
import ru.capjack.tool.biser.generator.model.ListType
import ru.capjack.tool.biser.generator.model.MapType
import ru.capjack.tool.biser.generator.model.NullableType
import ru.capjack.tool.biser.generator.model.PrimitiveType
import ru.capjack.tool.biser.generator.model.TypeVisitor

class TsTypeNameVisitor : TypeVisitor<String, DependedCode> {
	override fun visitPrimitiveType(type: PrimitiveType, data: DependedCode): String {
		return when (type) {
			PrimitiveType.BOOLEAN       -> "boolean"
			PrimitiveType.BYTE          -> "number"
			PrimitiveType.INT           -> "number"
			PrimitiveType.LONG          -> {
				data.addDependency("ru.capjack.tool.lang/Long")
				"Long"
			}
			PrimitiveType.DOUBLE        -> "number"
			PrimitiveType.STRING        -> "string"
			PrimitiveType.BOOLEAN_ARRAY -> "boolean[]"
			PrimitiveType.BYTE_ARRAY    -> "Int8Array"
			PrimitiveType.INT_ARRAY     -> "number[]"
			PrimitiveType.LONG_ARRAY    -> "Long[]"
			PrimitiveType.DOUBLE_ARRAY  -> "number[]"
		}
	}
	
	override fun visitEntityType(type: EntityType, data: DependedCode): String {
		data.addDependency(type.name)
		return type.name.internal.joinToString("_")
	}
	
	override fun visitListType(type: ListType, data: DependedCode): String {
		return "${type.element.accept(this, data)}[]"
	}
	
	override fun visitMapType(type: MapType, data: DependedCode): String {
		return "Map<${type.key.accept(this, data)}, ${type.value.accept(this, data)}>"
	}
	
	override fun visitNullableType(type: NullableType, data: DependedCode): String {
		return type.original.accept(this, data) + " | null"
	}
	
}
