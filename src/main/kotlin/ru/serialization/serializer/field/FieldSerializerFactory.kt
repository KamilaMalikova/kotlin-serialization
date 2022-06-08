package ru.serialization.serializer.field

import ru.serialization.Configuration
import ru.serialization.serializer.BaseSerializerFactory
import ru.serialization.serializer.Serializer
import ru.serialization.utils.isPrimitive
import kotlin.reflect.KClass

class FieldSerializerFactory(
    val config: FieldSerializerConfig = FieldSerializerConfig()
): BaseSerializerFactory() {

    override fun <T : Any> newSerializer(configuration: Configuration, type: KClass<T>): Serializer<T> =
        if (type.isPrimitive()) throw IllegalArgumentException("type cannot be a primitive class: $type")
        else FieldSerializer(configuration, type)
}

