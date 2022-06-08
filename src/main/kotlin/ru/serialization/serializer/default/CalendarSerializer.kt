package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import java.util.*
import kotlin.reflect.KClass

class CalendarSerializer(
    private val timeZoneSerializer: TimeZoneSerializer = TimeZoneSerializer()
): Serializer<Calendar>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: Calendar) {
        with(output) {
            timeZoneSerializer.write(configuration, output, serializableValue.timeZone)

            writeVarLong(serializableValue.timeInMillis, true)
            writeBoolean(serializableValue.isLenient)
            writeInt(serializableValue.firstDayOfWeek, true)
            writeInt(serializableValue.minimalDaysInFirstWeek, true)
            if (serializableValue is GregorianCalendar) {
                output.writeVarLong(serializableValue.gregorianChange.time, false)
            } else {
                output.writeVarLong(DEFAULT_GREGORIAN_CUTOVER, false)
            }
        }

    }

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): Calendar =
        Calendar.getInstance(timeZoneSerializer.read(configuration, input, TimeZone::class))
            .apply {
                timeInMillis = input.readVarLong(true)
                isLenient = input.readBoolean()
                firstDayOfWeek = input.readInt(true)
                minimalDaysInFirstWeek = input.readInt(true)

                input.readVarLong(false).takeIf { it != DEFAULT_GREGORIAN_CUTOVER }
                    ?.let { if (this is GregorianCalendar) gregorianChange = Date(it) }
            }

    private companion object {
        const val DEFAULT_GREGORIAN_CUTOVER = -12219292800000L
    }
}