package ru.capjack.tool.biser.generator.langs.kotlin

import ru.capjack.tool.biser.generator.CodersGenerator
import ru.capjack.tool.biser.generator.CodersTypeAggregator
import ru.capjack.tool.biser.generator.code.Code
import ru.capjack.tool.biser.generator.model.EntityName
import ru.capjack.tool.biser.generator.model.Model
import ru.capjack.tool.biser.generator.model.Type
import ru.capjack.tool.biser.generator.model.TypeVisitor
import java.nio.file.Path

class KotlinCodersGenerator(
	private val targetPackage: String,
	private val internal: Boolean = false,
	private val encodersName: String? = null,
	private val decodersName: String? = null
) : CodersGenerator() {
	
	override fun generate(model: Model, targetSourceDir: Path) {
		val encoderNaming = KotlinCoderNaming(true)
		val decoderNaming = KotlinCoderNaming(false)
		val typeNames = KotlinTypeNameVisitor()
		
		generate(
			targetSourceDir,
			model.resolveEntityName(targetPackage, encodersName ?: "ModelEncoders"),
			model,
			encoders,
			KotlinEncoderGenerator(model, encoders, encoderNaming, typeNames, KotlinWriteCallVisitor(encoderNaming))
		)
		
		generate(
			targetSourceDir,
			model.resolveEntityName(targetPackage, decodersName ?: "ModelDecoders"),
			model,
			decoders,
			KotlinDecoderGenerator(model, decoders, decoderNaming, typeNames, KotlinReadCallVisitor(decoderNaming))
		)
	}
	
	private fun generate(targetSourceDir: Path, targetEntityName: EntityName, model: Model, types: Set<Type>, generator: TypeVisitor<Unit, Code>) {
		if (types.isEmpty()) {
			return
		}
		
		val allTypes = types.toMutableSet()
		val aggregator = CodersTypeAggregator(model)
		types.forEach { it.accept(aggregator, allTypes) }
		
		val codeFile = KotlinCodeFile(targetEntityName)
		val code = codeFile.body.identBracketsCurly((if (internal) "internal " else "") + "object " + targetEntityName.self + " ")
		
		allTypes.forEach { it.accept(generator, code) }
		
		codeFile.save(targetSourceDir)
	}
}