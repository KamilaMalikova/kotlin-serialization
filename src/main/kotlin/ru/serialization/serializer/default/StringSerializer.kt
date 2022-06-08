package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import java.math.BigDecimal
import kotlin.reflect.KClass

class StringSerializer: Serializer<String>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: String) =
        output.writeString(serializableValue)

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): String? =
        input.readString()
}