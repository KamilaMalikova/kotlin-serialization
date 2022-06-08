package ru.serialization.serializer.field

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import kotlin.reflect.KClass

class FieldSerializer<T>(
    val configuration: Configuration,
    val type: KClass<*>,
    val fieldSerializerConfig: FieldSerializerConfig = FieldSerializerConfig()
): Serializer<T>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: T) {
        TODO("Not yet implemented")
    }

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): T {
        TODO("Not yet implemented")
    }
}