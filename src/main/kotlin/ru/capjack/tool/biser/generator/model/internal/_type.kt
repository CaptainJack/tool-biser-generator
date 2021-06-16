package ru.capjack.tool.biser.generator.model.internal

import ru.capjack.tool.biser.generator.model.EntityName
import ru.capjack.tool.biser.generator.model.EntityType
import ru.capjack.tool.biser.generator.model.ListType
import ru.capjack.tool.biser.generator.model.MapType
import ru.capjack.tool.biser.generator.model.NullableType
import ru.capjack.tool.biser.generator.model.Type
import ru.capjack.tool.biser.generator.model.TypeVisitor
import java.util.*

internal class EntityTypeImpl(override val name: EntityName) : EntityType {
	override fun <R, D> accept(visitor: TypeVisitor<R, D>, data: D) = visitor.visitEntityType(this, data)
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is EntityTypeImpl) return false
		if (name != other.name) return false
		return true
	}
	
	override fun hashCode() = Objects.hash("<entity>", name)
	override fun toString() = "Entity<$name>"
}

internal class NullableTypeImpl(override val original: Type) : NullableType {
	override fun <R, D> accept(visitor: TypeVisitor<R, D>, data: D) = visitor.visitNullableType(this, data)
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is NullableType) return false
		if (original != other.original) return false
		return true
	}
	
	override fun hashCode() = Objects.hash("<null>", original)
	override fun toString() = "Nullable<$original>"
}

internal class ListTypeImpl(override val element: Type) : ListType {
	override fun <R, D> accept(visitor: TypeVisitor<R, D>, data: D) = visitor.visitListType(this, data)
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is ListType) return false
		if (element != other.element) return false
		return true
	}
	
	override fun hashCode() = Objects.hash("<list>", element)
	override fun toString() = "List<$element>"
}

internal class MapTypeImpl(override val key: Type, override val value: Type) : MapType {
	override fun <R, D> accept(visitor: TypeVisitor<R, D>, data: D) = visitor.visitMapType(this, data)
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is MapType) return false
		if (key != other.key) return false
		if (value != other.value) return false
		return true
	}
	
	override fun hashCode() = Objects.hash("<map>", key, value)
	override fun toString() = "Map<$key,$value>"
}