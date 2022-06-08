package ru.serialization.serializer.`class`

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import ru.serialization.utils.isPrimitive
import kotlin.reflect.KClass

class ClassSerializer: Serializer<KClass<*>>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: KClass<*>) {
        serializableValue.let {
            configuration.writeClass(output, it)
            output.writeBoolean(it.isPrimitive())
        }
    }

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): KClass<*>? =
        configuration.readClass(input)?.type
}