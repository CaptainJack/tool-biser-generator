package ru.capjack.tool.biser.generator.langs.kotlin

import ru.capjack.tool.biser.generator.model.DefaultModel
import ru.capjack.tool.biser.generator.model.Model
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinLoaderTest {
	@Test
	fun `Load to empty model`() {
		val model = DefaultModel()
		val source = CommonKotlinSource(Paths.get(javaClass.classLoader.getResource("stub.kt")!!.toURI()))
		
		val loader = KotlinLoader(source, model)
		loader.load(KotlinPackageFilter("biser"))
		
		assertEquals(Model.Mutation.COMPATIBLY, model.mutation)
		assertEquals(16, model.entities.size)
	}
}