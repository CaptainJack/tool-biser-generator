package ru.capjack.tool.biser.generator.langs.kotlin

import ru.capjack.tool.biser.generator.code.DependedCode
import ru.capjack.tool.biser.generator.model.EntityType
import ru.capjack.tool.biser.generator.model.ListType
import ru.capjack.tool.biser.generator.model.MapType
import ru.capjack.tool.biser.generator.model.NullableType
import ru.capjack.tool.biser.generator.model.PrimitiveType
import ru.capjack.tool.biser.generator.model.TypeVisitor

open class KotlinTypeNameVisitor : TypeVisitor<String, DependedCode> {
	override fun visitPrimitiveType(type: PrimitiveType, data: DependedCode): String {
		return when (type) {
			PrimitiveType.BOOLEAN       -> "Boolean"
			PrimitiveType.BYTE          -> "Byte"
			PrimitiveType.INT           -> "Int"
			PrimitiveType.LONG          -> "Long"
			PrimitiveType.DOUBLE        -> "Double"
			PrimitiveType.STRING        -> "String"
			PrimitiveType.BOOLEAN_ARRAY -> "BooleanArray"
			PrimitiveType.BYTE_ARRAY    -> "ByteArray"
			PrimitiveType.INT_ARRAY     -> "IntArray"
			PrimitiveType.LONG_ARRAY    -> "LongArray"
			PrimitiveType.DOUBLE_ARRAY  -> "DoubleArray"
		}
	}
	
	override fun visitEntityType(type: EntityType, data: DependedCode): String {
		data.addDependency(type.name)
		return type.name.internal.joinToString(".")
	}
	
	override fun visitListType(type: ListType, data: DependedCode): String {
		return "List<${type.element.accept(this, data)}>"
	}
	
	override fun visitMapType(type: MapType, data: DependedCode): String {
		return "Map<${type.key.accept(this, data)}, ${type.value.accept(this, data)}>"
	}
	
	override fun visitNullableType(type: NullableType, data: DependedCode): String {
		return type.original.accept(this, data) + '?'
	}
	
}