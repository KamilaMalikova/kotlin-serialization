package ru.serialization.integration

import com.hazelcast.core.HazelcastInstance
import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.InputChunked
import ru.serialization.io.Output
import ru.serialization.io.OutputChunked
import ru.serialization.utils.instanceDestroyed
import java.io.InputStream
import java.io.OutputStream
import java.lang.Boolean.getBoolean

abstract class Strategy<T>(val hazelcastInstance: HazelcastInstance) {

    private val KRYOS: ThreadLocal<Context> = object : ThreadLocal<Context>() {
        override fun initialValue(): Context {
            val configuration: Configuration = newConfigurationInstance()
            val output = OutputChunked()
            val input = InputChunked()
            return Context(BUFFER_SIZE,configuration, input, output)
        }
    }

    private fun newConfigurationInstance(): Configuration {
        TODO()
    }

    abstract fun registerCustomSerializers(configuration: Configuration)

    abstract fun newTypeId(): Int

    fun write(outputStream: OutputStream, serializableValue: T) =
        with(KRYOS.get()) {
            this.outputChunked.outputStream = outputStream
            writeObject(configuration, outputChunked, serializableValue)
        }

    abstract fun writeObject(configuration: Configuration, output: Output, serializableValue: T)

    fun read(inputStream: InputStream): T =
        with(KRYOS.get()) {
            inputChunked.inputStream = inputStream
            readObject(configuration, inputChunked)
        }

    abstract fun readObject(configuration: Configuration, input: Input): T

    fun destroy(hazelcastInstance: HazelcastInstance) {
        instanceDestroyed(hazelcastInstance)
    }

    private companion object {
        val BUFFER_SIZE = Integer.getInteger("serializer.buffer.size.kb", 16) * 1024
        val IGNORE_HAZELCAST_CLASSLOADER: Boolean = getBoolean("serializer.classloading.ignore")

        val DEFAULT_REFERENCE_RESOLVER_CLASS = "ru.serialization.kryo.util.MapReferenceResolver"
        val REFERENCE_RESOLVER_CLASS_SYSTEM_PROPERTY = "serializer.referenceresolver.class"

    }
}