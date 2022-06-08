package ru.serialization.serializer.default

import TestCase
import org.junit.Test
import ru.serialization.serializer.`class`.ClassSerializer
import kotlin.reflect.KClass

class IntSerializerTest: TestCase() {
    @Test
    fun `test int`() {
        configuration.register(Int::class, IntSerializer())
        roundTrip(2, 0)
        roundTrip(2, 63)
        roundTrip(3, 64)
        roundTrip(3, 127)
        roundTrip(3, 128)
        roundTrip(3, 8191)
        roundTrip(4, 8192)
        roundTrip(4, 16383)
        roundTrip(4, 16384)
        roundTrip(5, 2097151)
        roundTrip(4, 1048575)
        roundTrip(5, 134217727)
        roundTrip(6, 268435455)
        roundTrip(6, 134217728)
        roundTrip(6, 268435456)
        roundTrip(2, -64)
        roundTrip(3, -65)
        roundTrip(3, -8192)
        roundTrip(4, -1048576)
        roundTrip(5, -134217728)
        roundTrip(6, -134217729)
    }
}