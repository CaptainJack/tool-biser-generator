package ru.capjack.tool.biser.generator.model


interface Entity {
	val name: EntityName
	
	fun <R, D> accept(visitor: EntityVisitor<R, D>, data: D): R
	fun <R> accept(visitor: EntityVisitor<R, Unit>): R = accept(visitor, Unit)
	
	fun <R, D> accept(data: D, e: (EnumEntity, D) -> R, c: (ClassEntity, D) -> R, o: (ObjectEntity, D) -> R): R = accept(object : EntityVisitor<R, D> {
		override fun visitEnumEntity(entity: EnumEntity, data: D) = e(entity, data)
		override fun visitClassEntity(entity: ClassEntity, data: D) = c(entity, data)
		override fun visitObjectEntity(entity: ObjectEntity, data: D) = o(entity, data)
	}, data)
	
	fun <R> accept(e: (EnumEntity) -> R, c: (ClassEntity) -> R, o: (ObjectEntity) -> R): R = accept(object : EntityVisitor<R, Unit> {
		override fun visitEnumEntity(entity: EnumEntity, data: Unit) = e(entity)
		override fun visitClassEntity(entity: ClassEntity, data: Unit) = c(entity)
		override fun visitObjectEntity(entity: ObjectEntity, data: Unit) = o(entity)
	})
}

interface EnumEntity : Entity {
	val values: List<String>
}


interface InheritedEntity : Entity {
	val id: Int
	val parent: ClassEntity?
}

interface ClassEntity : InheritedEntity {
	val abstract: Boolean
	val sealed: Boolean
	val fields: List<Field>
	val children: Collection<InheritedEntity>
	
	data class Field(
		val name: String,
		val type: Type
	)
	
	val allChildren: Set<InheritedEntity>
}


interface ObjectEntity : InheritedEntity {
}


interface EntityVisitor<R, D> {
	fun visitEnumEntity(entity: EnumEntity, data: D): R
	fun visitClassEntity(entity: ClassEntity, data: D): R
	fun visitObjectEntity(entity: ObjectEntity, data: D): R
}
