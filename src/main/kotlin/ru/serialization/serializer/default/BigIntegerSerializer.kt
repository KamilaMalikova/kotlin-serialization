package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

class BigIntegerSerializer: Serializer<BigInteger>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: BigInteger) {
        if (serializableValue == BigInteger.ZERO) {
            output.writeByte(2)
            output.writeByte(0)
        } else {
            val bytes = serializableValue.toByteArray()
            output.writeVarInt(bytes.size+1)
            output.writeBytes(bytes)
        }
    }

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): BigInteger? {
        val length = input.readVarInt()
        val bytes: ByteArray = input.readBytes(length - 1)

        return if (length == 2) {
            when (bytes[0].toInt()) {
                0 -> BigInteger.ZERO
                1 -> BigInteger.ONE
                10 -> BigInteger.TEN
                else -> BigInteger(bytes)
            }
        } else BigInteger(bytes)

    }
}