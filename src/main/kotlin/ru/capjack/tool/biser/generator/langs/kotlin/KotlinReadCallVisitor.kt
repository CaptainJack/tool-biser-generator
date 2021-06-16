package ru.capjack.tool.biser.generator.langs.kotlin

import ru.capjack.tool.biser.generator.model.EntityType
import ru.capjack.tool.biser.generator.model.ListType
import ru.capjack.tool.biser.generator.model.MapType
import ru.capjack.tool.biser.generator.model.NullableType
import ru.capjack.tool.biser.generator.model.PrimitiveType
import ru.capjack.tool.biser.generator.model.TypeVisitor

open class KotlinReadCallVisitor(protected val encoderNaming: KotlinCoderNaming) : TypeVisitor<String, Unit> {
	
	override fun visitPrimitiveType(type: PrimitiveType, data: Unit): String {
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
	
	override fun visitListType(type: ListType, data: Unit): String {
		return "readList(${encoderNaming.resolveName(type.element)})"
	}
	
	override fun visitMapType(type: MapType, data: Unit): String {
		return "readMap(${encoderNaming.resolveName(type.key)}, ${encoderNaming.resolveName(type.value)})"
	}
	
	override fun visitEntityType(type: EntityType, data: Unit): String {
		return "read(${encoderNaming.resolveName(type)})"
	}
	
	override fun visitNullableType(type: NullableType, data: Unit): String {
		if (type.original == PrimitiveType.STRING) {
			return "readStringNullable()"
		}
		return "read(${encoderNaming.resolveName(type)})"
	}
	
}