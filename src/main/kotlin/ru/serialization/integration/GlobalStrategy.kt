package ru.serialization.integration

import com.hazelcast.core.HazelcastInstance
import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.utils.globalId

class GlobalStrategy<T: Any>(
    val userSerializer: UserSerializer,
    hazelcastInstance: HazelcastInstance
): Strategy<T>(hazelcastInstance) {

    override fun newTypeId(): Int = globalId(hazelcastInstance)

    override fun registerCustomSerializers(configuration: Configuration) =
        userSerializer.registerAllSerializers(configuration)

    override fun writeObject(configuration: Configuration, output: Output, serializableValue: T) {
        configuration.writeClassAndObject(output, serializableValue)
    }

    override fun readObject(configuration: Configuration, input: Input): T =
        configuration.readClassAndObject(input) as T
}