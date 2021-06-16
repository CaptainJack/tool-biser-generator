package ru.capjack.tool.biser.generator.model

interface Type {
	fun <R, D> accept(visitor: TypeVisitor<R, D>, data: D): R
	fun <R> accept(visitor: TypeVisitor<R, Unit>): R = accept(visitor, Unit)
}

enum class PrimitiveType : Type {
	BOOLEAN,
	BYTE,
	INT,
	LONG,
	DOUBLE,
	STRING,
	
	BOOLEAN_ARRAY,
	BYTE_ARRAY,
	INT_ARRAY,
	LONG_ARRAY,
	DOUBLE_ARRAY;
	
	override fun <R, D> accept(visitor: TypeVisitor<R, D>, data: D): R = visitor.visitPrimitiveType(this, data)
}

interface EntityType : Type {
	val name: EntityName
}

interface ListType : Type {
	val element: Type
}

interface MapType : Type {
	val key: Type
	val value: Type
}

interface NullableType : Type {
	val original: Type
}


interface TypeVisitor<R, D> {
	fun visitPrimitiveType(type: PrimitiveType, data: D): R
	fun visitEntityType(type: EntityType, data: D): R
	fun visitListType(type: ListType, data: D): R
	fun visitMapType(type: MapType, data: D): R
	fun visitNullableType(type: NullableType, data: D): R
}