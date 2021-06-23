package ru.capjack.tool.biser.generator.langs

import ru.capjack.tool.biser.generator.DependedCode
import ru.capjack.tool.biser.generator.model.EntityType
import ru.capjack.tool.biser.generator.model.ListType
import ru.capjack.tool.biser.generator.model.MapType
import ru.capjack.tool.biser.generator.model.NullableType
import ru.capjack.tool.biser.generator.model.PrimitiveType
import ru.capjack.tool.biser.generator.model.TypeVisitor

open class DefaultReadCallVisitor(protected val encoderNaming: TypeVisitor<String, DependedCode>) : TypeVisitor<String, DependedCode> {
	
	override fun visitPrimitiveType(type: PrimitiveType, data: DependedCode): String {
		return when (type) {
			PrimitiveType.BOOLEAN       -> "readBoolean()"
			PrimitiveType.BYTE          -> "readByte()"
			PrimitiveType.INT           -> "readInt()"
			PrimitiveType.LONG          -> "readLong()"
			PrimitiveType.DOUBLE        -> "readDouble()"
			PrimitiveType.STRING        -> "readString()"
			PrimitiveType.BOOLEAN_ARRAY -> "readBooleanArray()"
			PrimitiveType.BYTE_ARRAY    -> "readByteArray()"
			PrimitiveType.INT_ARRAY     -> "readIntArray()"
			PrimitiveType.LONG_ARRAY    -> "readLongArray()"
			PrimitiveType.DOUBLE_ARRAY  -> "readDoubleArray()"
		}
	}
	
	override fun visitListType(type: ListType, data: DependedCode): String {
		return "readList(${type.element.accept(encoderNaming, data)})"
	}
	
	override fun visitMapType(type: MapType, data: DependedCode): String {
		return "readMap(${type.key.accept(encoderNaming, data)}, ${type.value.accept(encoderNaming, data)})"
	}
	
	override fun visitEntityType(type: EntityType, data: DependedCode): String {
		return "read(${type.accept(encoderNaming, data)})"
	}
	
	override fun visitNullableType(type: NullableType, data: DependedCode): String {
		if (type.original == PrimitiveType.STRING) {
			return "readStringNullable()"
		}
		return "read(${type.original.accept(encoderNaming, data)})"
	}
	
}