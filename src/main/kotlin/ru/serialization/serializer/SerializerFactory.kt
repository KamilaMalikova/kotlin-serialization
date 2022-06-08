package ru.serialization.serializer

import ru.serialization.Configuration
import kotlin.reflect.KClass

interface SerializerFactory {
    fun isSupported(type: KClass<*>): Boolean
    fun <T : Any>newSerializer(configuration: Configuration, type: KClass<T>): Serializer<T>
}

abstract class BaseSerializerFactory: SerializerFactory {
    override fun isSupported(type: KClass<*>): Boolean = true
}