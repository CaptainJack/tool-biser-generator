package ru.capjack.tool.biser.generator.model

import ru.capjack.tool.biser.generator.model.internal.ClassEntityImpl
import ru.capjack.tool.biser.generator.model.internal.EntityTypeImpl
import ru.capjack.tool.biser.generator.model.internal.EnumEntityImpl
import ru.capjack.tool.biser.generator.model.internal.ListTypeImpl
import ru.capjack.tool.biser.generator.model.internal.MapTypeImpl
import ru.capjack.tool.biser.generator.model.internal.NullableTypeImpl
import ru.capjack.tool.biser.generator.model.internal.ObjectEntityImpl
import ru.capjack.tool.lang.cast
import ru.capjack.tool.utils.collections.MutableKeyedSet
import ru.capjack.tool.utils.collections.getOrAdd
import ru.capjack.tool.utils.collections.mutableKeyedSetOf

open class DefaultModel : Model {
	final override var mutation: Model.Mutation = Model.Mutation.ABSENT
		private set
	final override var nextEntityId = 1
		private set
	
	override val nameSpace = NameSpace()
	
	private val _entities = mutableKeyedSetOf(Entity::name)
	
	private val cacheEntityTypes = mutableKeyedSetOf<EntityName, EntityType> { it.name }
	private val cacheNullableTypes = mutableKeyedSetOf(NullableType::original)
	private val cacheListTypes = mutableKeyedSetOf(ListType::element)
	private val cacheMapTypes = hashMapOf<Type, MutableKeyedSet<Type, MapType>>()
	
	final override val entities: Collection<Entity>
		get() = _entities
	
	
	override fun commit(nextEntityId: Int) {
		require(nextEntityId >= this.nextEntityId)
		mutation = Model.Mutation.ABSENT
		this.nextEntityId = nextEntityId
	}
	
	override fun resolveEntityName(name: String): EntityName {
		val s = name.indexOf('/')
		require(s >= 0)
		return resolveEntityName(name.substring(0, s), name.substring(s + 1))
	}
	
	override fun resolveEntityName(external: String?, internal: String): EntityName {
		return if (external == null) nameSpace.resolveEntityName(internal) else nameSpace.resolvePackageName(external).resolveEntityName(internal)
	}
	
	override fun findEntity(name: EntityName): Entity? {
		return _entities[name]
	}
	
	override fun findClassEntity(name: EntityName): ClassEntity? {
		return findClassEntityImpl(name)
	}
	
	override fun findObjectEntity(name: EntityName): ObjectEntity? {
		return _entities[name]?.cast { "Name $name registered not as ObjectEntity" }
	}
	
	override fun findEnumEntity(name: EntityName): EnumEntity? {
		return _entities[name]?.cast { "Name $name registered not as EnumEntity" }
	}
	
	
	override fun getEntity(name: EntityName): Entity {
		return requireNotNull(findEntity(name)) { "Entity by name $name is not registered" }
	}
	
	override fun getClassEntity(name: EntityName): ClassEntity {
		return requireNotNull(findClassEntity(name)) { "Entity by name $name is not registered" }
	}
	
	override fun getObjectEntity(name: EntityName): ObjectEntity {
		return requireNotNull(findObjectEntity(name)) { "Entity by name $name is not registered" }
	}
	
	override fun getEnumEntity(name: EntityName): EnumEntity {
		return requireNotNull(findEnumEntity(name)) { "Entity by name $name is not registered" }
	}
	
	override fun resolveClassEntity(name: EntityName, parent: EntityName?, fields: List<ClassEntity.Field>, abstract: Boolean, sealed: Boolean): ClassEntity {
		return doResolveEntity<ClassEntity, ClassEntityImpl>(name,
			{ ClassEntityImpl(nextEntityId++, name, abstract, sealed, tryGetClassEntityImpl(parent), fields) },
			{ it.update(abstract, sealed, tryGetClassEntityImpl(parent), fields) }
		)
	}
	
	override fun resolveObjectEntity(name: EntityName, parent: EntityName?): ObjectEntity {
		return doResolveEntity<ObjectEntity, ObjectEntityImpl>(name,
			{ ObjectEntityImpl(nextEntityId++, name, tryGetClassEntityImpl(parent)) },
			{ it.update(tryGetClassEntityImpl(parent)) }
		)
	}
	
	override fun resolveEnumEntity(name: EntityName, values: List<String>): EnumEntity {
		return doResolveEntity<EnumEntity, EnumEntityImpl>(name,
			{ EnumEntityImpl(name, values) },
			{ it.update(values) }
		)
	}
	
	override fun resolveClassEntity(id: Int, name: EntityName, parent: EntityName?, fields: List<ClassEntity.Field>, abstract: Boolean, sealed: Boolean): ClassEntity {
		return doResolveEntity<ClassEntity, ClassEntityImpl>(id, name,
			{ ClassEntityImpl(id, name, abstract, sealed, tryGetClassEntityImpl(parent), fields) },
			{ it.update(id, abstract, sealed, tryGetClassEntityImpl(parent), fields) }
		)
	}
	
	override fun resolveObjectEntity(id: Int, name: EntityName, parent: EntityName?): ObjectEntity {
		return doResolveEntity<ObjectEntity, ObjectEntityImpl>(id, name,
			{ ObjectEntityImpl(id, name, tryGetClassEntityImpl(parent)) },
			{ it.update(id, tryGetClassEntityImpl(parent)) }
		)
	}
	
	private fun findClassEntityImpl(name: EntityName): ClassEntityImpl? {
		return _entities[name]?.cast { "Entity name $name registered not as ClassEntity" }
	}
	
	private fun tryGetClassEntityImpl(name: EntityName?): ClassEntityImpl? {
		if (name == null) return null
		return requireNotNull(findClassEntityImpl(name)) { "Entity by name $name is not registered" }
	}
	
	private inline fun <reified E : InheritedEntity, reified I : E> doResolveEntity(id: Int, name: EntityName, create: () -> I, update: (I) -> Boolean): E {
		return doResolveEntity<E, I>(name, {
			require(_entities.none { (it as? InheritedEntity)?.id == id }) { "Entity id $id already registered" }
			create()
		}, { e ->
			if (e.id != id) {
				require(_entities.none { (it as? InheritedEntity)?.id == id }) { "Entity id $id already registered" }
			}
			update(e)
		})
	}
	
	private inline fun <reified E : Entity, reified I : E> doResolveEntity(name: EntityName, create: () -> I, update: (I) -> Boolean): E {
		var entity = _entities[name]
		if (entity is I) {
			if (update(entity)) {
				raiseMutation(Model.Mutation.FULL)
			}
			return entity
		}
		
		if (entity == null) {
			entity = create()
			_entities.add(entity)
			raiseMutation(Model.Mutation.COMPATIBLY)
			return entity
		}
		
		throw IllegalStateException("Entity name $name already registered not as ${E::class.simpleName}")
	}
	
	override fun resolveType(type: String): Type {
		return if (type.endsWith('>')) {
			when {
				type.startsWith("Entity<") -> resolveEntityType(resolveEntityName(type.substring(7, type.length - 1))) // Entity<$name>
				type.startsWith("Nullable<") -> resolveNullableType(resolveType(type.substring(9, type.length - 1))) // Nullable<$original>
				type.startsWith("List<") -> resolveListType(resolveType(type.substring(5, type.length - 1))) // List<$element>
				type.startsWith("Map<") -> type.indexOf(',').let { //Map<$key,$value>
					resolveMapType(
						resolveType(type.substring(4, it)),
						resolveType(type.substring(it + 1, type.length - 1))
					)
				}
				else -> throw IllegalArgumentException("Illegal type $type")
			}
		}
		else {
			PrimitiveType.valueOf(type)
		}
	}
	
	override fun resolveEntityType(name: EntityName): EntityType {
		return cacheEntityTypes.getOrAdd(name) { EntityTypeImpl(name) }
	}
	
	override fun resolveNullableType(original: Type): NullableType {
		return cacheNullableTypes.getOrAdd(original, ::NullableTypeImpl)
	}
	
	override fun resolveListType(element: Type): ListType {
		return cacheListTypes.getOrAdd(element, ::ListTypeImpl)
	}
	
	override fun resolveMapType(key: Type, value: Type): MapType {
		return cacheMapTypes
			.getOrPut(key) { mutableKeyedSetOf(MapType::value) }
			.getOrAdd(value) { MapTypeImpl(key, value) }
	}
	
	private fun raiseMutation(mutation: Model.Mutation) {
		this.mutation = this.mutation.raiseTo(mutation)
	}
}
