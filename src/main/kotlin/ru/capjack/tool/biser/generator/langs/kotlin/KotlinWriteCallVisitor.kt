package ru.capjack.tool.biser.generator.langs.kotlin

import ru.capjack.tool.biser.generator.model.EntityType
import ru.capjack.tool.biser.generator.model.ListType
import ru.capjack.tool.biser.generator.model.MapType
import ru.capjack.tool.biser.generator.model.NullableType
import ru.capjack.tool.biser.generator.model.PrimitiveType
import ru.capjack.tool.biser.generator.model.TypeVisitor

open class KotlinWriteCallVisitor(protected val encoderNaming: KotlinCoderNaming) : TypeVisitor<String, String> {
	
	override fun visitPrimitiveType(type: PrimitiveType, data: String): String {
		return when (type) {
			PrimitiveType.BOOLEAN       -> "writeBoolean($data)"
			PrimitiveType.BYTE          -> "writeByte($data)"
			PrimitiveType.INT           -> "writeInt($data)"
			PrimitiveType.LONG          -> "writeLong($data)"
			PrimitiveType.DOUBLE        -> "writeDouble($data)"
			PrimitiveType.STRING        -> "writeString($data)"
			PrimitiveType.BOOLEAN_ARRAY -> "writeBooleanArray($data)"
			PrimitiveType.BYTE_ARRAY    -> "writeByteArray($data)"
			PrimitiveType.INT_ARRAY     -> "writeIntArray($data)"
			PrimitiveType.LONG_ARRAY    -> "writeLongArray($data)"
			PrimitiveType.DOUBLE_ARRAY  -> "writeDoubleArray($data)"
		}
	}
	
	override fun visitListType(type: ListType, data: String): String {
		return "writeList($data, ${encoderNaming.resolveName(type.element)})"
	}
	
	override fun visitMapType(type: MapType, data: String): String {
		return "writeMap($data, ${encoderNaming.resolveName(type.key)}, ${encoderNaming.resolveName(type.value)})"
	}
	
	override fun visitEntityType(type: EntityType, data: String): String {
		return "write($data, ${encoderNaming.resolveName(type)})"
	}
	
	override fun visitNullableType(type: NullableType, data: String): String {
		if (type.original == PrimitiveType.STRING) {
			return "writeStringNullable($data)"
		}
		return "write($data, ${encoderNaming.resolveName(type)})"
	}
}