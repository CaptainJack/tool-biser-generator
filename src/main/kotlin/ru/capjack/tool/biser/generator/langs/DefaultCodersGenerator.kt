@file:Suppress("LeakingThis")

package ru.capjack.tool.biser.generator.langs

import ru.capjack.tool.biser.generator.CoderNameScopeVisitor
import ru.capjack.tool.biser.generator.Code
import ru.capjack.tool.biser.generator.DependedCode
import ru.capjack.tool.biser.generator.model.EntityName
import ru.capjack.tool.biser.generator.model.EntityType
import ru.capjack.tool.biser.generator.model.ListType
import ru.capjack.tool.biser.generator.model.MapType
import ru.capjack.tool.biser.generator.model.Model
import ru.capjack.tool.biser.generator.model.NullableType
import ru.capjack.tool.biser.generator.model.PrimitiveType
import ru.capjack.tool.biser.generator.model.Type
import ru.capjack.tool.biser.generator.model.TypeVisitor
import java.nio.file.Path

abstract class DefaultCodersGenerator(
	protected val model: Model,
	targetPackage: String,
	encodersName: String? = null,
	decodersName: String? = null,
	biserEncodersEntityName: String,
	biserDecodersEntityName: String
) {
	
	protected val encoders = mutableSetOf<Type>()
	protected val decoders = mutableSetOf<Type>()
	
	protected val biserEncodersName = model.resolveEntityName(biserEncodersEntityName)
	protected val biserDecodersName = model.resolveEntityName(biserDecodersEntityName)
	
	protected val generatedEncodersName = model.resolveEntityName(targetPackage, encodersName ?: "ModelEncoders")
	protected val generatedDecodersName = model.resolveEntityName(targetPackage, decodersName ?: "ModelDecoders")
	
	private val outerWriteCallVisitor = DefaultWriteCallVisitor(DefaultCoderNameVisitor(createOuterCoderNameScopeVisitor(biserEncodersName, generatedEncodersName)))
	private val outerReadCallVisitor = DefaultReadCallVisitor(DefaultCoderNameVisitor(createOuterCoderNameScopeVisitor(biserDecodersName, generatedDecodersName)))
	
	
	protected abstract val typeNames: TypeVisitor<String, DependedCode>
	
	private val callRegistration = object : TypeVisitor<Unit, MutableSet<Type>> {
		override fun visitPrimitiveType(type: PrimitiveType, data: MutableSet<Type>) {}
		
		override fun visitEntityType(type: EntityType, data: MutableSet<Type>) {
			data.add(type)
		}
		
		override fun visitListType(type: ListType, data: MutableSet<Type>) {
			data.add(type.element)
		}
		
		override fun visitMapType(type: MapType, data: MutableSet<Type>) {
			data.add(type.key)
			data.add(type.value)
		}
		
		override fun visitNullableType(type: NullableType, data: MutableSet<Type>) {
			data.add(type)
		}
	}
	
	fun getTypeName(type: Type, depended: DependedCode): String {
		return type.accept(typeNames, depended)
	}
	
	fun provideWriteCall(depended: DependedCode, type: Type, value: String): String {
		type.accept(callRegistration, encoders)
		return outerWriteCallVisitor.visit(type, depended, value)
	}
	
	fun provideReadCall(depended: DependedCode, type: Type): String {
		type.accept(callRegistration, decoders)
		return type.accept(outerReadCallVisitor, depended)
	}
	
	fun registerEncoder(type: Type) {
		encoders.add(type)
	}
	
	fun registerDecoder(type: Type) {
		decoders.add(type)
	}
	
	open fun generate(targetSourceDir: Path) {
		
		val encoderNames = DefaultCoderNameVisitor(createInnerCoderNameScopeVisitor(biserEncodersName))
		val decoderNames = DefaultCoderNameVisitor(createInnerCoderNameScopeVisitor(biserDecodersName))
		
		generate(
			targetSourceDir,
			generatedEncodersName,
			encoders,
			createEncoderGenerator(model, encoders, encoderNames, typeNames)
		)
		
		generate(
			targetSourceDir,
			generatedDecodersName,
			decoders,
			createDecoderGenerator(model, decoders, decoderNames, typeNames)
		)
	}
	
	protected abstract fun createOuterCoderNameScopeVisitor(biserCodersName: EntityName, generatedCodersName: EntityName): CoderNameScopeVisitor
	
	protected abstract fun createInnerCoderNameScopeVisitor(biserCodersName: EntityName): CoderNameScopeVisitor
	
	protected abstract fun createEncoderGenerator(
		model: Model,
		encoders: MutableSet<Type>,
		encoderNames: DefaultCoderNameVisitor,
		typeNames: TypeVisitor<String, DependedCode>
	): TypeVisitor<Unit, Code>
	
	protected abstract fun createDecoderGenerator(
		model: Model,
		decoders: MutableSet<Type>,
		decoderNames: DefaultCoderNameVisitor,
		typeNames: TypeVisitor<String, DependedCode>
	): TypeVisitor<Unit, Code>
	
	protected abstract fun generate(targetSourceDir: Path, targetEntityName: EntityName, types: Set<Type>, generator: TypeVisitor<Unit, Code>)
}


