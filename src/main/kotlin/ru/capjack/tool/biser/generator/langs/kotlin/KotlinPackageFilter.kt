package ru.capjack.tool.biser.generator.langs.kotlin

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import java.util.function.Predicate

class KotlinPackageFilter(name: String) : Predicate<ClassDescriptor> {
	private val prefix = org.jetbrains.kotlin.name.Name.identifier(name)
	
	override fun test(descriptor: ClassDescriptor): Boolean {
		return descriptor.fqNameSafe.startsWith(prefix)
	}
}