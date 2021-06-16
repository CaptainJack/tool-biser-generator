package ru.capjack.tool.biser.generator.langs.kotlin

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.containingPackage
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isBoolean
import org.jetbrains.kotlin.types.typeUtil.isByte
import org.jetbrains.kotlin.types.typeUtil.isDouble
import org.jetbrains.kotlin.types.typeUtil.isEnum
import org.jetbrains.kotlin.types.typeUtil.isInt
import org.jetbrains.kotlin.types.typeUtil.isInterface
import org.jetbrains.kotlin.types.typeUtil.isLong
import ru.capjack.tool.biser.generator.model.ClassEntity
import ru.capjack.tool.biser.generator.model.EntityName
import ru.capjack.tool.biser.generator.model.EnumEntity
import ru.capjack.tool.biser.generator.model.Model
import ru.capjack.tool.biser.generator.model.ObjectEntity
import ru.capjack.tool.biser.generator.model.PrimitiveType
import ru.capjack.tool.biser.generator.model.Type
import java.util.function.Predicate


open class KotlinLoader<M : Model>(
	protected val source: KotlinSource,
	protected val model: M,
) {
	fun load(filter: Predicate<ClassDescriptor>) {
		source.classDescriptors.forEach {
			if (filter.test(it)) processClassDescriptor(it)
		}
	}
	
	protected open fun processClassDescriptor(descriptor: ClassDescriptor) {
		resolveName(descriptor)
		
		when (descriptor.kind) {
			ClassKind.CLASS      -> loadClassEntity(descriptor)
			ClassKind.OBJECT     -> loadObjectEntity(descriptor)
			ClassKind.ENUM_CLASS -> loadEnumEntity(descriptor)
			else                 -> Unit
		}
	}
	
	private fun loadClassEntity(descriptor: ClassDescriptor): ClassEntity {
		val name = resolveName(descriptor)
		val parent = descriptor.getSuperClassNotAny()?.let(::resolveClassEntity)
		val constructor = requireNotNull(descriptor.unsubstitutedPrimaryConstructor)
		val fields = constructor.valueParameters.map { ClassEntity.Field(it.name.identifier, resolveType(it.type)) }
		
		return model.resolveClassEntity(
			name,
			parent?.name,
			fields,
			descriptor.modality == Modality.ABSTRACT || descriptor.modality == Modality.SEALED,
			descriptor.modality == Modality.SEALED
		)
	}
	
	private fun loadObjectEntity(descriptor: ClassDescriptor): ObjectEntity {
		val name = resolveName(descriptor)
		val parent = descriptor.getSuperClassNotAny()?.let(::resolveClassEntity)
		
		return model.resolveObjectEntity(name, parent?.name)
	}
	
	private fun loadEnumEntity(descriptor: ClassDescriptor): EnumEntity {
		val name = resolveName(descriptor)
		val values = descriptor.unsubstitutedMemberScope.getContributedDescriptors()
			.asSequence()
			.filterIsInstance<ClassDescriptor>()
			.filter { it.kind == ClassKind.ENUM_ENTRY }
			.map { it.name.toString() }
			.toList()
		
		return model.resolveEnumEntity(name, values)
	}
	
	protected fun resolveName(descriptor: ClassifierDescriptor): EntityName {
		val packageName = descriptor.containingPackage()?.asString()
		val entityName = descriptor.fqNameSafe.asString().substring(packageName?.let { it.length + 1 } ?: 0)
		
		return model.resolveEntityName(packageName, entityName)
	}
	
	protected fun resolveClassEntity(descriptor: ClassDescriptor): ClassEntity {
		return model.findClassEntity(resolveName(descriptor)) ?: loadClassEntity(descriptor)
	}
	
	protected fun resolveType(type: KotlinType): Type {
		return when {
			type.isBoolean()                              -> PrimitiveType.BOOLEAN
			type.isByte()                                 -> PrimitiveType.BYTE
			type.isInt()                                  -> PrimitiveType.INT
			type.isLong()                                 -> PrimitiveType.LONG
			type.isDouble()                               -> PrimitiveType.DOUBLE
			KotlinBuiltIns.isString(type)                 -> PrimitiveType.STRING
			KotlinBuiltIns.isStringOrNullableString(type) -> model.resolveNullableType(PrimitiveType.STRING)
			
			KotlinBuiltIns.isPrimitiveArray(type)         -> when (KotlinBuiltIns.getPrimitiveArrayType(requireNotNull(type.constructor.declarationDescriptor))) {
				org.jetbrains.kotlin.builtins.PrimitiveType.BOOLEAN -> PrimitiveType.BOOLEAN_ARRAY
				org.jetbrains.kotlin.builtins.PrimitiveType.BYTE    -> PrimitiveType.BYTE_ARRAY
				org.jetbrains.kotlin.builtins.PrimitiveType.INT     -> PrimitiveType.INT_ARRAY
				org.jetbrains.kotlin.builtins.PrimitiveType.LONG    -> PrimitiveType.LONG_ARRAY
				org.jetbrains.kotlin.builtins.PrimitiveType.DOUBLE  -> PrimitiveType.DOUBLE_ARRAY
				else                                                -> throw IllegalArgumentException("Unsupported primitive array type ($type)")
			}
			
			KotlinBuiltIns.isListOrNullableList(type)     -> {
				require(!type.isMarkedNullable) { "Nullable list is unsupported ($type)" }
				val elementType = type.arguments.first().type
				
				require(!elementType.isMarkedNullable) { "Nullable element on list is unsupported ($type)" }
				
				model.resolveListType(resolveType(elementType))
			}
			
			KotlinBuiltIns.isMapOrNullableMap(type)       -> {
				require(!type.isMarkedNullable) { "Nullable map is unsupported ($type)" }
				
				val keyType = type.arguments.first().type
				val valueType = type.arguments.last().type
				
				require(!keyType.isMarkedNullable) { "Nullable key on map is unsupported ($type)" }
				require(!valueType.isMarkedNullable) { "Nullable value on map is unsupported ($type)" }
				
				model.resolveMapType(resolveType(keyType), resolveType(valueType))
			}
			
			else                                          -> {
				val name = resolveName(requireNotNull(type.constructor.declarationDescriptor))
				
				if (type.isEnum()) {
					require(!type.isMarkedNullable) { "Nullable enum is unsupported ($type)" }
					model.resolveEntityType(name)
				}
				else if (!type.isInterface()) {
					val t = model.resolveEntityType(name)
					if (type.isMarkedNullable) model.resolveNullableType(t) else t
				}
				else {
					throw IllegalArgumentException("Unsupported type ($type)")
				}
			}
		}
	}
}