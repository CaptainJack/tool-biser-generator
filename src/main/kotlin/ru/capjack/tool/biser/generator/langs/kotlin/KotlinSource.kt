package ru.capjack.tool.biser.generator.langs.kotlin

import org.jetbrains.kotlin.descriptors.ClassDescriptor

interface KotlinSource {
	val classDescriptors: Collection<ClassDescriptor>
}
