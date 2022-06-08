package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import java.lang.reflect.Constructor
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass

class BigDecimalSerializer(
    private val bigIntegerSerializer: BigIntegerSerializer = BigIntegerSerializer()
): Serializer<BigDecimal>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: BigDecimal) {
        if (serializableValue === BigDecimal.ZERO) {
            bigIntegerSerializer.write(configuration, output, BigInteger.ZERO)
            output.writeInt(0, false) // for backwards compatibility
            return
        }
        bigIntegerSerializer.write(configuration, output, serializableValue.unscaledValue())
        output.writeInt(serializableValue.scale(), false)
    }

    override fun read(
        configuration: Configuration,
        input: Input, type: KClass<out Any>
    ): BigDecimal? {
        val unscaledValue = bigIntegerSerializer.read(configuration, input, BigInteger::class)
        val scale = input.readInt(false)
        return if (unscaledValue === BigInteger.ZERO && scale == 0) BigDecimal.ZERO else BigDecimal(unscaledValue, scale)
    }
}