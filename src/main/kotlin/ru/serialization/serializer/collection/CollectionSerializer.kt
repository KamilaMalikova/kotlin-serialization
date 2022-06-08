package ru.serialization.serializer.collection

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import kotlin.reflect.KClass

class CollectionSerializer<T: Collection<Any?>>(
    val elementType: KClass<out Any>? = null,
    val elementSerializer: Serializer<out Any>? = null,
    val elementCanBeNull: Boolean = true
): Serializer<T>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: T) {
        serializableValue.size
        val length: Int = serializableValue.size
        if (length == 0) {
            output.writeByte(1)
            writeHeader(configuration, output, serializableValue)
            return
        }

        var elementsCanBeNull: Boolean = this.elementCanBeNull
        var elementSerializer = elementSerializer
        if (elementSerializer == null) {
            val genericClass: KClass<*>? = configuration.getGenerics().nextGenericClass()
            if (genericClass != null && configuration.isFinal(genericClass))
                elementSerializer = configuration.getSerializer(genericClass)
        }

        elementSerializer?.let {
            if (elementsCanBeNull) {
                val isNull = serializableValue.any { it == null }
                output.writeVarIntFlag(isNull, length + 1, true)
                elementsCanBeNull = isNull
            } else {
                output.writeVarInt(length + 1, true)
            }
            writeHeader(configuration, output, serializableValue)
        } ?: {
            var elementType: KClass<out Any>? = null
            var hasNull = false

        }
    }

    /** Can be overidden to write data needed for object creation. The default implementation does
     * nothing. */
    protected fun writeHeader(configuration: Configuration, output: Output, serializableValue: T) {
    }

    /** Used by [.read] to create the new object. This can be overridden to customize object creation (eg
     * to call a constructor with arguments), optionally reading bytes written in [.writeHeader].
     * The default implementation uses [Kryo.newInstance] with special cases for ArrayList and HashSet.  */
    protected fun create(configuration: Configuration, input: Input, type: Class<out T>, size: Int): T {
        if (type == ArrayList::class.java) return ArrayList<Any>(size) as T
        if (type == HashSet::class.java) return HashSet<Any>(Math.max((size / 0.75f).toInt() + 1, 16)) as T
//        val collection: T = configuration.newInstance(type)
//        if (collection is ArrayList<*>) (collection as ArrayList<*>).ensureCapacity(size)
//        return collection
        TODO("Not yey implemented")
    }

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): T? {
        TODO("Not yet implemented")
    }
}