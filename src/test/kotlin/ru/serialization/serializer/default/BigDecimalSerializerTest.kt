package ru.serialization.serializer.default

import TestCase
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger

class BigDecimalSerializerTest: TestCase() {
    @Test
    fun `test BigDecimal`() {
        configuration.register(BigDecimal::class, BigDecimalSerializer())
        roundTrip(5, BigDecimal.valueOf(12345, 2))
        roundTrip(7, BigDecimal("12345.12345"))
        roundTrip(4, BigDecimal.ZERO)
        roundTrip(4, BigDecimal.ONE)
        roundTrip(4, BigDecimal.TEN)
    }
}