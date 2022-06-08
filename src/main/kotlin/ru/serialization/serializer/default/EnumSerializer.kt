package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import kotlin.reflect.KClass

class EnumSerializer: Serializer<Enum<*>>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: Enum<*>) {
        TODO("Not yet implemented")
    }

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): Enum<*>? {
        TODO("Not yet implemented")
    }
}