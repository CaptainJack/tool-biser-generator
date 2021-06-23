package ru.capjack.tool.biser.generator

import ru.capjack.tool.biser.generator.model.*

class CodersTypeAggregator(private val model: Model) : TypeVisitor<Unit, MutableSet<Type>>, EntityVisitor<Unit, MutableSet<Type>> {
	
	private val fieldsAggregator = object : TypeVisitor<Unit, MutableSet<Type>> {
		override fun visitPrimitiveType(type: PrimitiveType, data: MutableSet<Type>) {
		}
		
		override fun visitEntityType(type: EntityType, data: MutableSet<Type>) {
			if (data.add(type)) {
				model.getEntity(type.name).accept(this@CodersTypeAggregator, data)
			}
		}
		
		override fun visitListType(type: ListType, data: MutableSet<Type>) {
			type.element.accept(this@CodersTypeAggregator, data)
		}
		
		override fun visitMapType(type: MapType, data: MutableSet<Type>) {
			type.key.accept(this@CodersTypeAggregator, data)
			type.value.accept(this@CodersTypeAggregator, data)
		}
		
		override fun visitNullableType(type: NullableType, data: MutableSet<Type>) {
			if (data.add(type)) {
				type.original.accept(this@CodersTypeAggregator, data)
			}
		}
	}
	
	override fun visitPrimitiveType(type: PrimitiveType, data: MutableSet<Type>) {}
	
	override fun visitEntityType(type: EntityType, data: MutableSet<Type>) {
		if (data.add(type)) {
			model.getEntity(type.name).accept(this, data)
		}
	}
	
	override fun visitListType(type: ListType, data: MutableSet<Type>) {
		if (data.add(type)) {
			type.element.accept(this, data)
		}
	}
	
	override fun visitMapType(type: MapType, data: MutableSet<Type>) {
		if (data.add(type)) {
			type.key.accept(this, data)
			type.value.accept(this, data)
		}
	}
	
	override fun visitNullableType(type: NullableType, data: MutableSet<Type>) {
		if (data.add(type)) {
			type.original.accept(this, data)
		}
	}
	
	///
	
	override fun visitEnumEntity(entity: EnumEntity, data: MutableSet<Type>) {
	}
	
	override fun visitClassEntity(entity: ClassEntity, data: MutableSet<Type>) {
		entity.children.forEach {
			model.resolveEntityType(it.name).accept(this, data)
		}
		entity.fields.forEach {
			it.type.accept(fieldsAggregator, data)
		}
	}
	
	override fun visitObjectEntity(entity: ObjectEntity, data: MutableSet<Type>) {
	}
}

