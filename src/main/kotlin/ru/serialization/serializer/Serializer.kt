package ru.serialization.serializer

import ru.serialization.Configuration
import ru.serialization.io.Output
import ru.serialization.io.Input
import kotlin.reflect.KClass

abstract class Serializer<T> {
    abstract fun write(configuration: Configuration,output: Output, serializableValue: T)

    abstract fun read(
        configuration: Configuration,
        input: Input,
        type: KClass<out Any>
    ): T?
}