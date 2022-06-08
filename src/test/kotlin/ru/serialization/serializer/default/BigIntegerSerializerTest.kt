package ru.serialization.serializer.default

import TestCase
import org.junit.Test
import java.math.BigInteger

class BigIntegerSerializerTest: TestCase() {
    @Test
    fun `test BigInteger`() {
        configuration.register(BigInteger::class, BigIntegerSerializer())
        roundTrip(8, BigInteger.valueOf(1270507903945L))
        roundTrip(3, BigInteger.ZERO)
        roundTrip(3, BigInteger.ONE)
        roundTrip(3, BigInteger.TEN)
    }
}