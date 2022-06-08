package ru.serialization

import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import ru.serialization.serializer.SerializerFactory
import ru.serialization.serializer.field.FieldSerializerFactory
import ru.serialization.utils.Generics
import ru.serialization.utils.isAssignableFrom
import ru.serialization.utils.isAssignableToEnumSet
import ru.serialization.utils.kClass
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

class Configuration{
    private val classResolver: ClassResolver = DefaultClassResolver(this)
    private val defaultSerializers = mutableListOf<DefaultSerializerEntry>()
    private val defaultSerializer: SerializerFactory = FieldSerializerFactory()
    var nextRegisterId: Int = 0

    private fun getNextRegistrationId(): Int {
        while (true) {
            if (!classResolver.hasRegistrationWithId(nextRegisterId)) return nextRegisterId
            nextRegisterId++
        }
    }

    fun <T: Any> register(type: KClass<T>, serializer: Serializer<T>): Registration =
        classResolver.register(Registration(type, serializer, getNextRegistrationId()))

    fun <T : Any>getRegistration(type: KClass<T>): Registration =
        with(classResolver) {
            getRegistration(type)
                ?: let {
                    type.supertypes
                    if (type.isAssignableToEnumSet()) {
                        getRegistration(EnumSet::class)
                    } else {
                        type.superclasses.firstOrNull() { containsRegistration(it) }
                            ?.let { getRegistration(it) }
                            ?: registerImplicit(type)
                    }!!
                }
        }

    inline fun <reified T: Any> writeClassAndObject(output: Output, value: T? = null) {
        value?.let {
            with(writeClass(output, it::class)) {
                getSerializer(T::class).write(this@Configuration, output, it)
            }
        } ?: writeClass(output, T::class.kClass)
    }

    fun <T: Any> writeClass(output: Output, type: KClass<T>): Registration =
        classResolver.writeClass(output, type) ?: throw IllegalArgumentException("Registration is required")

    fun readClassAndObject(input: Input): Any? =
        with(readClass(input)) {
            this?.let {
                it.getSerializer(it.type).read(this@Configuration, input, it.type)
            }
        }

    fun readClass(input: Input): Registration? =
        classResolver.readClass(input)
            .also { /* add autoreset */ }

    fun getDefaultSerializer(type: KClass<*>): Serializer<*> {
        defaultSerializers.forEach {
            if (it.type.isAssignableFrom(type) && it.serializerFactory.isSupported(type))
                return it.serializerFactory.newSerializer(this, type)
        }
        return newDefaultSerializer(type)
    }

    private fun newDefaultSerializer(type: KClass<*>): Serializer<*> =
        defaultSerializer.newSerializer(this, type)

    fun getGenerics(): Generics {
        TODO("Not yet implemented")
    }

    fun isFinal(genericClass: KClass<*>): Boolean {
        TODO("Not yet implemented")
    }

    fun getSerializer(genericClass: KClass<*>): Serializer<out Any>? {
        TODO("Not yet implemented")
    }

    data class DefaultSerializerEntry(
        val type: KClass<*>,
        val serializerFactory: SerializerFactory
    )
}