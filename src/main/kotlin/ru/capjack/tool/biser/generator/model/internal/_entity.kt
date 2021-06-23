package ru.capjack.tool.biser.generator.model.internal

import ru.capjack.tool.biser.generator.model.ClassEntity
import ru.capjack.tool.biser.generator.model.Entity
import ru.capjack.tool.biser.generator.model.EntityName
import ru.capjack.tool.biser.generator.model.EntityVisitor
import ru.capjack.tool.biser.generator.model.EnumEntity
import ru.capjack.tool.biser.generator.model.InheritedEntity
import ru.capjack.tool.biser.generator.model.ObjectEntity
import ru.capjack.tool.lang.alsoTrue


internal abstract class AbstractEntity(override val name: EntityName) : Entity {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Entity) return false
		if (name != other.name) return false
		return true
	}
	
	override fun hashCode(): Int = name.hashCode()
}

internal abstract class AbstractInheritedEntity(id: Int, name: EntityName, parent: ClassEntityImpl?) : AbstractEntity(name), InheritedEntity {
	private var _id = id
	private var _parent: ClassEntityImpl? = parent
	
	override val id: Int
		get() = _id
	
	override val parent: ClassEntity?
		get() = _parent
	
	override val parents: List<ClassEntity>
		get() = _parent?.let { it.parents + it } ?: emptyList()
	
	init {
		@Suppress("LeakingThis")
		_parent?.children?.add(this)
	}
	
	protected fun updateId(id: Int): Boolean {
		return alsoTrue(_id != id) {
			_id = id
		}
	}
	
	protected fun updateParent(parent: ClassEntityImpl?): Boolean {
		return alsoTrue(_parent != parent) {
			_parent?.children?.remove(this)
			_parent = parent
			_parent?.children?.add(this)
		}
	}
}

internal class EnumEntityImpl(name: EntityName, values: List<String>) : AbstractEntity(name), EnumEntity {
	private val _values = values.toMutableList()
	
	override val values: List<String>
		get() = _values
	
	fun update(values: List<String>): Boolean {
		var changed = _values.size != values.size
		
		if (!changed) {
			for (i in values.indices) {
				if (values[i] != _values[i]) {
					changed = true
					break
				}
			}
		}
		if (changed) {
			_values.clear()
			_values.addAll(values)
		}
		
		return changed
	}
	
	override fun <R, D> accept(visitor: EntityVisitor<R, D>, data: D) = visitor.visitEnumEntity(this, data)
	
	override fun toString() = "EnumEntity($name)"
}


internal class ClassEntityImpl(
	id: Int,
	name: EntityName,
	abstract: Boolean,
	sealed: Boolean,
	parent: ClassEntityImpl?,
	fields: List<ClassEntity.Field>
) : AbstractInheritedEntity(id, name, parent), ClassEntity {
	
	private val _fields = fields.toMutableList()
	private var _abstract: Boolean = abstract
	private var _sealed: Boolean = sealed
	override val children = HashSet<AbstractInheritedEntity>()
	
	override val abstract: Boolean
		get() = _abstract
	
	override val sealed: Boolean
		get() = _sealed
	
	override val fields: List<ClassEntity.Field>
		get() = _fields
	
	override val allChildren: Set<InheritedEntity>
		get() = children + children.flatMap { (it as? ClassEntity)?.allChildren.orEmpty() }.toSet()
	
	override fun isFieldOwner(name: String): Boolean {
		if (parent?.isFieldOwner(name) == true) return false
		
		return _fields.any { it.name == name }
	}
	
	fun update(abstract: Boolean, sealed: Boolean, parent: ClassEntityImpl?, fields: List<ClassEntity.Field>): Boolean {
		var changed = updateParent(parent)
		
		if (_abstract != abstract) {
			changed = true
			_abstract = true
		}
		
		if (_sealed != sealed) {
			changed = true
			_sealed = true
		}
		
		var fieldsChanged = _fields.size != fields.size
		if (!fieldsChanged) {
			for (i in fields.indices) {
				if (fields[i] != _fields[i]) {
					fieldsChanged = true
					break
				}
			}
		}
		if (fieldsChanged) {
			changed = true
			_fields.clear()
			_fields.addAll(fields)
		}
		
		return changed
	}
	
	fun update(id: Int, abstract: Boolean, sealed: Boolean, parent: ClassEntityImpl?, fields: List<ClassEntity.Field>): Boolean {
		val changed1 = updateId(id)
		val changed2 = update(abstract, sealed, parent, fields)
		return changed1 || changed2
	}
	
	override fun <R, D> accept(visitor: EntityVisitor<R, D>, data: D) = visitor.visitClassEntity(this, data)
	
	override fun toString() = "ClassEntity($name)"
}


internal class ObjectEntityImpl(
	id: Int,
	name: EntityName,
	parent: ClassEntityImpl?
) : AbstractInheritedEntity(id, name, parent), ObjectEntity {
	
	
	fun update(parent: ClassEntityImpl?): Boolean {
		return updateParent(parent)
	}
	
	fun update(id: Int, parent: ClassEntityImpl?): Boolean {
		val changed1 = updateId(id)
		val changed2 = update(parent)
		return changed1 || changed2
	}
	
	override fun <R, D> accept(visitor: EntityVisitor<R, D>, data: D) = visitor.visitObjectEntity(this, data)
	
	override fun toString() = "ObjectEntity($name)"
}