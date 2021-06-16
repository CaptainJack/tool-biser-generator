package ru.capjack.tool.biser.generator.model

interface Model {
	val nameSpace: NameSpace
	val mutation: Mutation
	val nextEntityId: Int
	val entities: Collection<Entity>
	
	fun commit(nextEntityId: Int)
	
	
	fun resolveEntityName(name: String): EntityName
	
	fun resolveEntityName(external: String?, internal: String): EntityName
	
	fun findEntity(name: EntityName): Entity?
	
	fun findClassEntity(name: EntityName): ClassEntity?
	
	fun findObjectEntity(name: EntityName): ObjectEntity?
	
	fun findEnumEntity(name: EntityName): EnumEntity?
	
	
	fun getEntity(name: EntityName): Entity
	
	fun getClassEntity(name: EntityName): ClassEntity
	
	fun getObjectEntity(name: EntityName): ObjectEntity
	
	fun getEnumEntity(name: EntityName): EnumEntity
	
	
	fun resolveClassEntity(name: EntityName, parent: EntityName?, fields: List<ClassEntity.Field>, abstract: Boolean, sealed: Boolean): ClassEntity
	
	fun resolveObjectEntity(name: EntityName, parent: EntityName?): ObjectEntity
	
	fun resolveEnumEntity(name: EntityName, values: List<String>): EnumEntity
	
	
	fun resolveClassEntity(id: Int, name: EntityName, parent: EntityName?, fields: List<ClassEntity.Field>, abstract: Boolean, sealed: Boolean): ClassEntity
	
	fun resolveObjectEntity(id: Int, name: EntityName, parent: EntityName?): ObjectEntity
	
	
	fun resolveType(type: String): Type
	
	fun resolveEntityType(name: EntityName): EntityType
	
	fun resolveNullableType(original: Type): NullableType
	
	fun resolveListType(element: Type): ListType
	
	fun resolveMapType(key: Type, value: Type): MapType
	
	
	
	enum class Mutation {
		ABSENT,
		COMPATIBLY,
		FULL;
		
		fun raiseTo(mutation: Mutation): Mutation {
			return if (mutation.ordinal > ordinal) mutation else mutation
		}
	}
}