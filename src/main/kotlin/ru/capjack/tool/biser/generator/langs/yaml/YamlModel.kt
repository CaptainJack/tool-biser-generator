package ru.capjack.tool.biser.generator.langs.yaml

open class YamlModel {
	
	var lastEntityId: Int = 0
	val entities: Entities = Entities()
	
	class Entities(
		val enums: MutableList<EnumEntity> = mutableListOf(),
		val classes: MutableList<ClassEntity> = mutableListOf(),
		val objects: MutableList<ObjectEntity> = mutableListOf(),
	) {
		
		class EnumEntity(
			val name: String,
			val values: List<String>
		)
		
		class ClassEntity(
			val id: Int,
			val name: String,
			val parent: String? = null,
			val abstract: Boolean = false,
			val sealed: Boolean = false,
			val fields: List<Field>
		) {
			class Field(
				val name: String,
				val type: String,
				val readonly: Boolean
			)
		}
		
		class ObjectEntity(
			val id: Int,
			val name: String,
			val parent: String? = null
		)
	}
}
