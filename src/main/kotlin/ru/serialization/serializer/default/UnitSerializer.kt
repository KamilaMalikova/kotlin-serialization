package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import java.math.BigDecimal
import kotlin.reflect.KClass

class UnitSerializer: Serializer<Unit>() {
    override fun write(
        configuration: Configuration,
        output: Output,
        serializableValue: Unit
    ) { }

    override fun read(
        configuration: Configuration,
        input: Input,
        type: KClass<out Any>
    ): Unit? { return null}
}