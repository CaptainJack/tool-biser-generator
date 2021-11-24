package ru.capjack.tool.biser.generator.langs.kotlin

import ru.capjack.tool.biser.generator.CoderNameScopeVisitor
import ru.capjack.tool.biser.generator.CodersTypeAggregator
import ru.capjack.tool.biser.generator.Code
import ru.capjack.tool.biser.generator.DependedCode
import ru.capjack.tool.biser.generator.langs.DefaultCoderNameVisitor
import ru.capjack.tool.biser.generator.langs.AbstractCodersGenerator
import ru.capjack.tool.biser.generator.model.EntityName
import ru.capjack.tool.biser.generator.model.Model
import ru.capjack.tool.biser.generator.model.Type
import ru.capjack.tool.biser.generator.model.TypeVisitor
import java.nio.file.Path

class KotlinCodersGenerator(
	model: Model,
	targetPackage: String,
	encodersName: String? = null,
	decodersName: String? = null,
	private val internal: Boolean = false,
) : AbstractCodersGenerator<KotlinCodeSource>(
	model,
	targetPackage,
	encodersName,
	decodersName,
	"ru.capjack.tool.biser/Encoders",
	"ru.capjack.tool.biser/Decoders"
) {
	override val typeNames = KotlinTypeNameVisitor()
	
	override fun createOuterCoderNameScopeVisitor(biserCodersName: EntityName, generatedCodersName: EntityName): CoderNameScopeVisitor {
		return OuterCoderNameScopeVisitor(biserCodersName, generatedCodersName)
	}
	
	override fun createInnerCoderNameScopeVisitor(biserCodersName: EntityName): CoderNameScopeVisitor {
		return InnerCoderNameScopeVisitor(biserCodersName)
	}
	
	override fun createEncoderGenerator(
		model: Model,
		encoders: MutableSet<Type>,
		encoderNames: DefaultCoderNameVisitor,
		typeNames: TypeVisitor<String, DependedCode>
	): TypeVisitor<Unit, Code> {
		return KotlinEncoderGenerator(model, encoders, encoderNames, typeNames)
	}
	
	override fun createDecoderGenerator(
		model: Model,
		decoders: MutableSet<Type>,
		decoderNames: DefaultCoderNameVisitor,
		typeNames: TypeVisitor<String, DependedCode>
	): TypeVisitor<Unit, Code> {
		return KotlinDecoderGenerator(model, decoders, decoderNames, typeNames)
	}
	
	override fun generate(source: KotlinCodeSource, targetEntityName: EntityName, types: Set<Type>, generator: TypeVisitor<Unit, Code>) {
		if (types.isEmpty()) {
			return
		}
		
		val allTypes = mutableSetOf<Type>()
		val aggregator = CodersTypeAggregator(model)
		types.forEach { it.accept(aggregator, allTypes) }
		
		val codeFile = source.newFile(targetEntityName)
		val code = codeFile.body.identBracketsCurly((if (internal) "internal " else "") + "object " + targetEntityName.self + " ")
		
		allTypes.forEach { it.accept(generator, code) }
	}
	
	///
	
	private class OuterCoderNameScopeVisitor(private val biserCodersName: EntityName, private val generatedCodersName: EntityName) : CoderNameScopeVisitor {
		override fun visitPrimitiveScope(name: String, depended: DependedCode): String {
			depended.addDependency(biserCodersName)
			return "${biserCodersName.self}.$name"
		}
		
		override fun visitGeneratedScope(name: String, depended: DependedCode): String {
			depended.addDependency(generatedCodersName)
			return generatedCodersName.self + '.' + name
		}
	}
	
	private class InnerCoderNameScopeVisitor(private val biserCodersName: EntityName) : CoderNameScopeVisitor {
		override fun visitPrimitiveScope(name: String, depended: DependedCode): String {
			depended.addDependency(biserCodersName)
			return "${biserCodersName.self}.$name"
		}
		
		override fun visitGeneratedScope(name: String, depended: DependedCode): String {
			return name
		}
	}
}