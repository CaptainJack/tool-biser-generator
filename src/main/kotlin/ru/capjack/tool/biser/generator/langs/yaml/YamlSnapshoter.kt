package ru.capjack.tool.biser.generator.langs.yaml

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import ru.capjack.tool.biser.generator.model.ClassEntity
import ru.capjack.tool.biser.generator.model.EntityVisitor
import ru.capjack.tool.biser.generator.model.EnumEntity
import ru.capjack.tool.biser.generator.model.InheritedEntity
import ru.capjack.tool.biser.generator.model.Model
import ru.capjack.tool.biser.generator.model.ObjectEntity
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor


open class YamlSnapshoter<Y : YamlModel, M : Model>(private val ymClass: KClass<Y>) {
	companion object {
		fun createDefault(): YamlSnapshoter<YamlModel, Model> {
			return YamlSnapshoter(YamlModel::class)
		}
	}
	
	private val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)).registerKotlinModule()
	
	fun load(model: M, source: File) {
		val json = if (source.exists()) mapper.readValue(source, ymClass.java) else ymClass.primaryConstructor!!.call()
		load(model, json)
	}
	
	fun load(model: M, source: String) {
		val json = mapper.readValue(source, ymClass.java)
		load(model, json)
	}
	
	fun save(model: M, file: File) {
		val json = ymClass.primaryConstructor!!.call()
		save(model, json)
		mapper.writeValue(file, json)
	}
	
	fun save(model: M): String {
		val json = ymClass.primaryConstructor!!.call()
		save(model, json)
		return mapper.writeValueAsString(json)
	}
	
	protected open fun save(model: M, json: Y) {
		json.lastEntityId = model.lastEntityId
		
		val visitor = object : EntityVisitor<Unit, Y> {
			override fun visitEnumEntity(entity: EnumEntity, data: Y) {
				data.entities.enums.add(YamlModel.Entities.EnumEntity(entity.name.toString(), entity.values))
			}
			
			override fun visitClassEntity(entity: ClassEntity, data: Y) {
				data.entities.classes.add(YamlModel.Entities.ClassEntity(
					entity.id,
					entity.name.toString(),
					entity.parent?.name?.toString(),
					entity.abstract,
					entity.sealed,
					entity.fields.map {
						YamlModel.Entities.ClassEntity.Field(it.name, it.type.toString())
					}
				))
			}
			
			override fun visitObjectEntity(entity: ObjectEntity, data: Y) {
				data.entities.objects.add(
					YamlModel.Entities.ObjectEntity(
						entity.id,
						entity.name.toString(),
						entity.parent?.name?.toString(),
					)
				)
			}
		}
		
		model.entities
			.sortedBy { if (it is InheritedEntity) it.id * (it.parents.size * 100).coerceAtLeast(1) else 0 }
			.forEach { it.accept(visitor, json) }
	}
	
	protected open fun load(model: M, json: Y) {
		
		json.entities.enums.forEach {
			model.provideEnumEntity(model.resolveEntityName(it.name), it.values)
		}
		
		json.entities.classes.forEach { e ->
			model.provideClassEntity(
				e.id,
				model.resolveEntityName(e.name),
				e.parent?.let(model::resolveEntityName),
				e.fields.map {
					ClassEntity.Field(
						it.name,
						model.resolveType(it.type)
					)
				},
				e.abstract,
				e.sealed
			)
		}
		
		json.entities.objects.forEach {
			model.provideObjectEntity(
				it.id,
				model.resolveEntityName(it.name),
				it.parent?.let(model::resolveEntityName)
			)
		}
		
		model.commit(json.lastEntityId)
	}
	
}