package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.exception.SerializationException
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import java.sql.Time
import java.sql.Timestamp
import java.util.*
import kotlin.reflect.KClass

class DateSerializer: Serializer<Date>() {
    @Throws(SerializationException::class)
    private fun create(
        configuration: Configuration,
        time: Long,
        type: KClass<out Date>
    ): Date {
        if (type == Date::class) {
            return Date(time)
        }
        if (type == Timestamp::class) {
            return Timestamp(time)
        }
        if (type == java.sql.Date::class) {
            return java.sql.Date(time)
        }
        return if (type == Time::class) {
            Time(time)
        } else try {
            // Try to avoid invoking the no-args constructor
            // (which is expected to initialize the instance with the current time)
            type.constructors.first { it.parameters.size == 1 }
                .call(Long::class.javaPrimitiveType)
        } catch (ex: Exception) {
            Date(time)
        }
    }

    override fun write(configuration: Configuration, output: Output, serializableValue: Date) {
        output.writeVarLong(serializableValue.time, true)
    }

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): Date =
        create(configuration, input.readVarLong(true), type!! as KClass<out Date>)

}