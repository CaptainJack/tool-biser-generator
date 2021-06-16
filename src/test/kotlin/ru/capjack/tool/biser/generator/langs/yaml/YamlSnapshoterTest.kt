package ru.capjack.tool.biser.generator.langs.yaml

import org.junit.Test
import ru.capjack.tool.biser.generator.getResourcePath
import ru.capjack.tool.biser.generator.langs.kotlin.CommonKotlinSource
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinLoader
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinPackageFilter
import ru.capjack.tool.biser.generator.model.DefaultModel
import ru.capjack.tool.biser.generator.readResource
import java.io.File
import kotlin.test.assertEquals

class YamlSnapshoterTest {
	@Test
	fun `Save empty model to string`() {
		val model = DefaultModel()
		val converter = YamlSnapshoter.createDefault()
		
		val actual = converter.save(model)
		val expected = readResource("model-empty.yml")
		
		assertEquals(expected, actual)
	}
	
	@Test
	fun `Save empty model to file`() {
		val model = DefaultModel()
		val converter = YamlSnapshoter.createDefault()
		
		val file = File.createTempFile("tool-biser-generator", "yml")
		try {
			converter.save(model, file)
			val actual = file.readText()
			val expected = readResource("model-empty.yml")
			
			assertEquals(expected, actual)
		}
		finally {
			file.delete()
		}
	}
	
	@Test
	fun `Save filled model to file`() {
		val model = DefaultModel()
		val source = CommonKotlinSource(getResourcePath("stub.kt"))
		val loader = KotlinLoader(source, model)
		loader.load(KotlinPackageFilter("biser"))
		
		val converter = YamlSnapshoter.createDefault()
		
		val file = File.createTempFile("tool-biser-generator", "yml")
		try {
			converter.save(model, file)
			val actual = file.readText()
			val expected = readResource("model-filled.yml")
			
			File("tmp/kaka.yml").writeText(actual)
			
			assertEquals(expected, actual)
		}
		finally {
			file.delete()
		}
	}
}