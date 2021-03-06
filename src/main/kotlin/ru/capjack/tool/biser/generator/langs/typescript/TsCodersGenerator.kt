package ru.capjack.tool.biser.generator.langs.typescript

import ru.capjack.tool.biser.generator.Code
import ru.capjack.tool.biser.generator.CodeSource
import ru.capjack.tool.biser.generator.CoderNameScopeVisitor
import ru.capjack.tool.biser.generator.CodersTypeAggregator
import ru.capjack.tool.biser.generator.DependedCode
import ru.capjack.tool.biser.generator.langs.DefaultCoderNameVisitor
import ru.capjack.tool.biser.generator.langs.AbstractCodersGenerator
import ru.capjack.tool.biser.generator.model.*

class TsCodersGenerator(
	model: Model,
	targetPackage: String,
	encodersName: String? = null,
	decodersName: String? = null,
	private val generateSources: Boolean = true
) : AbstractCodersGenerator(
	model,
	targetPackage,
	encodersName,
	decodersName,
	"ru.capjack.tool.biser/Encoders",
	"ru.capjack.tool.biser/Decoders"
) {
	override val typeNames: TypeVisitor<String, DependedCode> = TsTypeNameVisitor()
	
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
		return TsEncoderGenerator(model, encoders, encoderNames, typeNames)
	}
	
	override fun createDecoderGenerator(
		model: Model,
		decoders: MutableSet<Type>,
		decoderNames: DefaultCoderNameVisitor,
		typeNames: TypeVisitor<String, DependedCode>
	): TypeVisitor<Unit, Code> {
		return TsDecoderGenerator(model, decoders, decoderNames, typeNames)
	}
	
	override fun generate(source: CodeSource) {
		super.generate(source)
		
		if (generateSources) {
			
			val entities = mutableSetOf<Entity>()
			val aggregator = object : TypeVisitor<Unit, MutableSet<Entity>>, EntityVisitor<Unit, MutableSet<Entity>> {
				override fun visitPrimitiveType(type: PrimitiveType, data: MutableSet<Entity>) {}
				
				override fun visitEntityType(type: EntityType, data: MutableSet<Entity>) {
					val entity = model.getEntity(type.name)
					entity.accept(this, data)
				}
				
				override fun visitListType(type: ListType, data: MutableSet<Entity>) {
					type.element.accept(this, data)
				}
				
				override fun visitMapType(type: MapType, data: MutableSet<Entity>) {
					type.key.accept(this, data)
					type.value.accept(this, data)
				}
				
				override fun visitNullableType(type: NullableType, data: MutableSet<Entity>) {
					type.original.accept(this, data)
				}
				
				override fun visitEnumEntity(entity: EnumEntity, data: MutableSet<Entity>) {
					data.add(entity)
				}
				
				override fun visitClassEntity(entity: ClassEntity, data: MutableSet<Entity>) {
					if (data.add(entity)) {
						entity.parent?.accept(this, data)
						entity.fields.forEach { it.type.accept(this, data) }
						entity.children.forEach { it.accept(this, data) }
					}
				}
				
				override fun visitObjectEntity(entity: ObjectEntity, data: MutableSet<Entity>) {
					if (data.add(entity)) {
						entity.parent?.accept(this, data)
					}
				}
			}
			
			encoders.forEach { it.accept(aggregator, entities) }
			decoders.forEach { it.accept(aggregator, entities) }
			
			val generator = TsSourceGenerator(typeNames)
			
			entities.forEach {
				val file = source.newFile(it.name)
				it.accept(generator, file.body)
			}
		}
	}
	
	override fun generate(source: CodeSource, targetEntityName: EntityName, types: Set<Type>, generator: TypeVisitor<Unit, Code>) {
		if (types.isEmpty()) {
			return
		}
		
		val allTypes = mutableSetOf<Type>()
		val aggregator = CodersTypeAggregator(model)
		types.forEach { it.accept(aggregator, allTypes) }
		
		val file = source.newFile(targetEntityName)
		file.header.line("// noinspection DuplicatedCode,JSUnusedLocalSymbols")
		file.header.line()
		val code = file.body.identBracketsCurly("export namespace " + targetEntityName.self + " ")
		
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