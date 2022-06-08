package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import kotlin.reflect.KClass

class CharSerializer: Serializer<Char>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: Char) =
        output.writeChar(serializableValue)

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): Char =
        input.readChar()
}