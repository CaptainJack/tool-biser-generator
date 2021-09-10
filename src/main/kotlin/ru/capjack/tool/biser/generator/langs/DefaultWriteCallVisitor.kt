package ru.capjack.tool.biser.generator.langs

import ru.capjack.tool.biser.generator.DependedCode
import ru.capjack.tool.biser.generator.model.EntityType
import ru.capjack.tool.biser.generator.model.ListType
import ru.capjack.tool.biser.generator.model.MapType
import ru.capjack.tool.biser.generator.model.NullableType
import ru.capjack.tool.biser.generator.model.PrimitiveType
import ru.capjack.tool.biser.generator.model.Type
import ru.capjack.tool.biser.generator.model.TypeVisitor

class DefaultWriteCallVisitor(private val encoderNaming: TypeVisitor<String, DependedCode>) : TypeVisitor<String, DefaultWriteCallVisitor.Data> {
	
	override fun visitPrimitiveType(type: PrimitiveType, data: Data): String {
		return when (type) {
			PrimitiveType.BOOLEAN       -> "writeBoolean("
			PrimitiveType.BYTE          -> "writeByte("
			PrimitiveType.INT           -> "writeInt("
			PrimitiveType.LONG          -> "writeLong("
			PrimitiveType.DOUBLE        -> "writeDouble("
			PrimitiveType.STRING        -> "writeString("
			PrimitiveType.BOOLEAN_ARRAY -> "writeBooleanArray("
			PrimitiveType.BYTE_ARRAY    -> "writeByteArray("
			PrimitiveType.INT_ARRAY     -> "writeIntArray("
			PrimitiveType.LONG_ARRAY    -> "writeLongArray("
			PrimitiveType.DOUBLE_ARRAY  -> "writeDoubleArray("
		} + "${data.value})"
	}
	
	override fun visitListType(type: ListType, data: Data): String {
		return "writeList(${data.value}, ${type.element.accept(encoderNaming, data.code)})"
	}
	
	override fun visitMapType(type: MapType, data: Data): String {
		return "writeMap(${data.value}, ${type.key.accept(encoderNaming, data.code)}, ${type.value.accept(encoderNaming, data.code)})"
	}
	
	override fun visitEntityType(type: EntityType, data: Data): String {
		return "write(${data.value}, ${type.accept(encoderNaming, data.code)})"
	}
	
	override fun visitNullableType(type: NullableType, data: Data): String {
		if (type.original == PrimitiveType.STRING) {
			return "writeStringNullable(${data.value})"
		}
		return "write(${data.value}, ${type.accept(encoderNaming, data.code)})"
	}
	
	fun visit(type: Type, code: DependedCode, value: String): String {
		return type.accept(this, Data(code, value))
	}
	
	
	class Data(
		val code: DependedCode,
		val value: String
	)
}