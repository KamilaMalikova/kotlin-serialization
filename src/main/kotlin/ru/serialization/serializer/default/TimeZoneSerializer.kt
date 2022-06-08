package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import java.util.*
import kotlin.reflect.KClass

class TimeZoneSerializer: Serializer<TimeZone>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: TimeZone) {
        output.writeString(serializableValue.id)
    }

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): TimeZone? =
        TimeZone.getTimeZone(input.readString())
}