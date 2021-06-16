package ru.capjack.tool.biser.generator.model

import ru.capjack.tool.lang.cast
import ru.capjack.tool.utils.collections.getOrAdd
import ru.capjack.tool.utils.collections.mutableKeyedSetOf


open class SeparatedNameContainer(protected val namesParent: SeparatedName?) {
	protected val names = mutableKeyedSetOf(SeparatedName::self)
	
	fun resolveEntityName(name: String, separator: String = "."): EntityName {
		val i = name.indexOf(separator)
		return if (i == -1) resolveEntityName0(name)
		else resolveEntityName0(name.substring(0, i)).resolveEntityName(name.substring(i + separator.length))
	}
	
	private fun resolveEntityName0(name: String): EntityName {
		return names.getOrAdd(name) { EntityName(it, namesParent) }.cast { "Name $it is already registered as Package" }
	}
}

open class PackageNameContainer(namesParent: SeparatedName?) : SeparatedNameContainer(namesParent) {
	
	fun resolvePackageName(name: String, separator: String = "."): PackageName {
		val i = name.indexOf(separator)
		return if (i == -1) resolvePackageName0(name)
		else resolvePackageName0(name.substring(0, i)).resolvePackageName(name.substring(i + separator.length))
	}
	
	private fun resolvePackageName0(name: String): PackageName {
		return names.getOrAdd(name) { PackageName(it, namesParent) }.cast { "Name $it is already registered as Entity" }
	}
}

abstract class SeparatedName(
	val self: String,
	val parent: SeparatedName? = null
) {
	protected abstract val children: SeparatedNameContainer
	
	val full: List<String> by lazy {
		(this.parent?.full ?: emptyList()) + this.self
	}
	
	fun resolveEntityName(name: String, separator: String = "."): EntityName {
		return children.resolveEntityName(name, separator)
	}
	
	fun toString(separator: String): String {
		val parts = mutableListOf<String>()
		var p: SeparatedName? = this
		while (p != null) {
			parts.add(p.self)
			p = p.parent
		}
		return parts.reversed().joinToString(separator)
	}
	
	override fun toString(): String {
		return toString(".")
	}
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is SeparatedName) return false
		if (self != other.self) return false
		if (parent != other.parent) return false
		return true
	}
	
	override fun hashCode(): Int {
		var result = self.hashCode()
		result = 31 * result + (parent?.hashCode() ?: 0)
		return result
	}
}

class EntityName(name: String, parent: SeparatedName? = null) : SeparatedName(name, parent) {
	override val children = SeparatedNameContainer(this)
	
	val external: List<String> by lazy {
		this.parent?.let { if (it is EntityName) it.external else it.full } ?: emptyList()
	}
	
	val internal: List<String> by lazy {
		((this.parent as? EntityName)?.internal ?: emptyList()) + this.self
	}
	
	val path: List<String> by lazy {
		external + internal.first()
	}
}

class PackageName(name: String, parent: SeparatedName? = null) : SeparatedName(name, parent) {
	override val children = PackageNameContainer(this)
	
	fun resolvePackageName(name: String, separator: String = "."): PackageName {
		return children.resolvePackageName(name, separator)
	}
}

class NameSpace {
	private val names = PackageNameContainer(null)
	
	fun resolveEntityName(name: String, separator: String = "."): EntityName {
		return names.resolveEntityName(name, separator)
	}
	
	fun resolvePackageName(name: String, separator: String = "."): PackageName {
		return names.resolvePackageName(name, separator)
	}
}
