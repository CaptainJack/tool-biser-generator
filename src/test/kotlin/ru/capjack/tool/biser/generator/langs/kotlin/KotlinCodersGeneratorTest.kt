package ru.capjack.tool.biser.generator.langs.kotlin

import ru.capjack.tool.biser.generator.model.DefaultModel
import java.nio.file.Paths
import kotlin.test.Test

class KotlinCodersGeneratorTest {
	
	@Test
	fun generate() {
		val model = DefaultModel()
		val source = CommonKotlinSource(Paths.get(javaClass.classLoader.getResource("stub.kt")!!.toURI()!!))
		
		val loader = KotlinLoader(source, model)
		loader.load(KotlinPackageFilter("biser"))
		
		val generator = KotlinCodersGenerator("biser")
		
		model.entities.forEach {
			val type = model.resolveEntityType(it.name)
			generator.registerEncoder(type)
			generator.registerDecoder(type)
		}
		
		generator.generate(model, Paths.get("src/test/resources/tmp"))
	}
}