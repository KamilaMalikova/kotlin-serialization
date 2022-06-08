package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import java.sql.Timestamp
import kotlin.reflect.KClass

class TimestampSerializer: Serializer<Timestamp>() {
    private fun integralTimeComponent(timestamp: Timestamp): Long {
        return timestamp.time - timestamp.nanos / 1000000
    }

    override fun write(configuration: Configuration, output: Output, serializableValue: Timestamp) {
        with(output) {
            writeVarLong(integralTimeComponent(serializableValue), true)
            writeVarInt(serializableValue.nanos, true)
        }
    }

    private fun create(time: Long, nanos: Int): Timestamp = Timestamp(time).also { it.nanos = nanos }

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): Timestamp =
        with(input) { create(readVarLong(true), readVarInt(true)) }
}