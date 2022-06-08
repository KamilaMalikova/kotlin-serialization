package ru.serialization

import ru.serialization.io.Input
import ru.serialization.io.Output
import kotlin.reflect.KClass

class DefaultClassResolver (
    private val configuration: Configuration
): ClassResolver {
    private val classToNameId: Map<String, Int> = HashMap()
    private val idToRegistration: MutableMap<Int, Registration> = mutableMapOf()
    private val classToRegistration: MutableMap<KClass<out Any>, Registration> = mutableMapOf()
    private val nameIdToClass: MutableMap<Int, KClass<out Any>> = mutableMapOf()
    private val nameToClass: MutableMap<String, KClass<out Any>> = mutableMapOf()

    override fun <T : Any> writeClass(output: Output, type: KClass<T>?): Registration? =
        type?.let {
            val registration = configuration.getRegistration(it)
            if (registration.id == NAME) {
                writeName(output, type, registration)
            } else {
                output.writeVarInt(registration.id + 2)
            }
            registration
        } ?: let {
            output.writeByte(NULL)
            return null
        }

    private fun <T: Any> writeName(output: Output, type: KClass<T>, registration: Registration) {
        output.writeByte(1) // NAME + 2

    }

    override fun hasRegistrationWithId(registerId: Int): Boolean =
        idToRegistration.containsKey(registerId)

    override fun register(registration: Registration): Registration {
        if (registration.id != NAME) {
            idToRegistration[registration.id] = registration
        }
        classToRegistration[registration.type] = registration
        val wrapperClass = getWrapperClass(registration.type)
        if(wrapperClass != registration.type) classToRegistration[wrapperClass] = registration
        return registration
    }

    /** Returns the primitive wrapper class for a primitive class, or the specified class if it is not primitive.  */
    private fun getWrapperClass(type: KClass<*>): KClass<*> {
        if (type == Int::class) return Int::class
        if (type == Float::class) return Float::class
        if (type == Boolean::class) return Boolean::class
        if (type == Byte::class) return Byte::class
        if (type == Long::class) return Long::class
        if (type == Char::class) return Char::class
        if (type == Double::class) return Double::class
        return if (type == Short::class) Short::class else type
    }

    override fun readClass(input: Input): Registration? =
        input.readVarInt(true).let {
            when(it) {
                0 -> return null
                NAME + 2 -> readName(input)
                else -> idToRegistration[it - 2]
            }
        }

    private fun readName(input: Input): Registration {
        val typeId = input.readVarInt(true)
        return nameIdToClass[typeId]
            ?.let { configuration.getRegistration(it) }
            ?: let {
                val typeName = input.readString() ?: ""
                val type = getTypeByName(typeName)
                    ?: Class.forName(typeName, false, Configuration::class.java.classLoader).kotlin
                nameToClass[typeName] = type
                nameIdToClass[typeId] = type
                configuration.getRegistration(type)
            }
    }

    private fun getTypeByName(className: String): KClass<out Any>? =
        nameToClass[className]

    override fun <T : Any> getRegistration(type: KClass<T>): Registration? =
        classToRegistration[type]

    override fun <T : Any> containsRegistration(type: KClass<T>): Boolean =
        classToRegistration.containsKey(type)

    override fun registerImplicit(type: KClass<*>): Registration =
        register(Registration(type, configuration.getDefaultSerializer(type), NAME))

    companion object {
        val NULL: Byte = 0
        val NAME = -1
    }
}