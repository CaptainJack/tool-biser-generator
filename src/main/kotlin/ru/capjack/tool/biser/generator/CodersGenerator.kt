package ru.capjack.tool.biser.generator

import ru.capjack.tool.biser.generator.model.Model
import ru.capjack.tool.biser.generator.model.Type
import java.nio.file.Path

abstract class CodersGenerator {
	
	protected val encoders = mutableSetOf<Type>()
	protected val decoders = mutableSetOf<Type>()
	
	fun registerEncoder(type: Type) {
		encoders.add(type)
	}
	
	fun registerDecoder(type: Type) {
		decoders.add(type)
	}
	
	abstract fun generate(model: Model, targetSourceDir: Path)
}


