package ru.serialization

import org.objenesis.instantiator.ObjectInstantiator
import ru.serialization.serializer.Serializer
import kotlin.reflect.KClass

class Registration(
    val type: KClass<*>,
    val serializer: Serializer<*>,
    val id: Int,
    var instantiator: ObjectInstantiator<*>? = null
) {
    fun <T: Any> getSerializer(type: KClass<T>): Serializer<T> = serializer as Serializer<T>
}