package ru.capjack.tool.biser.generator.langs

import ru.capjack.tool.biser.generator.CoderNameScopeVisitor
import ru.capjack.tool.biser.generator.DependedCode
import ru.capjack.tool.biser.generator.model.EntityType
import ru.capjack.tool.biser.generator.model.ListType
import ru.capjack.tool.biser.generator.model.MapType
import ru.capjack.tool.biser.generator.model.NullableType
import ru.capjack.tool.biser.generator.model.PrimitiveType
import ru.capjack.tool.biser.generator.model.TypeVisitor

class DefaultCoderNameVisitor(private val scope: CoderNameScopeVisitor) : TypeVisitor<String, DependedCode> {
	private var deep = 0
	
	override fun visitPrimitiveType(type: PrimitiveType, data: DependedCode): String {
		val name = when (type) {
			PrimitiveType.BOOLEAN       -> "BOOLEAN"
			PrimitiveType.BYTE          -> "BYTE"
			PrimitiveType.INT           -> "INT"
			PrimitiveType.LONG          -> "LONG"
			PrimitiveType.DOUBLE        -> "DOUBLE"
			PrimitiveType.STRING        -> "STRING"
			PrimitiveType.BOOLEAN_ARRAY -> "BOOLEAN_ARRAY"
			PrimitiveType.BYTE_ARRAY    -> "BYTE_ARRAY"
			PrimitiveType.INT_ARRAY     -> "INT_ARRAY"
			PrimitiveType.LONG_ARRAY    -> "LONG_ARRAY"
			PrimitiveType.DOUBLE_ARRAY  -> "DOUBLE_ARRAY"
		}
		return if (deep == 0) scope.visitPrimitiveScope(name, data) else name
	}
	
	override fun visitListType(type: ListType, data: DependedCode): String {
		++deep
		val name = "LIST_" + type.element.accept(this, data)
		--deep
		return if (deep == 0) scope.visitGeneratedScope(name, data) else name
	}
	
	override fun visitMapType(type: MapType, data: DependedCode): String {
		++deep
		val name = "MAP_" + type.key.accept(this, data) + "__" + type.value.accept(this, data)
		--deep
		return if (deep == 0) scope.visitGeneratedScope(name, data) else name
	}
	
	override fun visitEntityType(type: EntityType, data: DependedCode): String {
		val name = "ENTITY_" + type.name.internal.joinToString("_")
		return if (deep == 0) scope.visitGeneratedScope(name, data) else name
	}
	
	override fun visitNullableType(type: NullableType, data: DependedCode): String {
		++deep
		val name = "NULLABLE_" + type.original.accept(this, data)
		--deep
		return if (deep == 0) scope.visitGeneratedScope(name, data) else name
	}
}

