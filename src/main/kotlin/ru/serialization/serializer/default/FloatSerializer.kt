package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import java.math.BigDecimal
import kotlin.reflect.KClass

class FloatSerializer: Serializer<Float>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: Float) {
        output.writeFloat(serializableValue)
    }

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): Float =
        input.readFloat()
}