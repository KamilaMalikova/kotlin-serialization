package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import java.math.BigDecimal
import kotlin.reflect.KClass

class LongSerializer: Serializer<Long>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: Long){
        output.writeVarLong(serializableValue, false)
    }

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): Long =
        input.readVarLong(false)
}