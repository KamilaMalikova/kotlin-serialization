import ru.serialization.Configuration
import ru.serialization.io.DefaultInput
import ru.serialization.io.DefaultOutput
import ru.serialization.io.Input
import ru.serialization.io.Output
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass
import kotlin.test.assertEquals

open class TestCase {
    val configuration = Configuration()
    lateinit var output: Output
    lateinit var input: Input

    interface BufferFactory {
        fun createOutput(os: OutputStream): Output

        fun createOutput(os: OutputStream, size: Int): Output

        fun createOutput(size: Int, limit: Int): Output

        fun createInput(os: InputStream, size: Int): Input

        fun createInput(buffer: ByteArray): Input
    }

    class DefaultBufferFactory: BufferFactory {
        override fun createOutput(os: OutputStream): Output = DefaultOutput(os)

        override fun createOutput(os: OutputStream, size: Int): Output =
            DefaultOutput(os, size)

        override fun createOutput(size: Int, limit: Int): Output {
            TODO("Not yet implemented")
        }

        override fun createInput(os: InputStream, size: Int): Input =
            DefaultInput(os, size)

        override fun createInput(buffer: ByteArray): Input {
            TODO("Not yet implemented")
        }
    }
    /**
     * Creates buffer
     */
    inline fun <reified T> roundTrip(length: Int, object1: T) {
        //val object2:
        roundTripWithBufferFactory(length, object1, DefaultBufferFactory())
    }

    inline fun <reified T> roundTripWithBufferFactory(length: Int, object1: T, bf: BufferFactory) {
        val outStream = ByteArrayOutputStream();
        // Test output
        output = bf.createOutput(outStream, 4096)
        configuration.writeClassAndObject(output, object1)
        output.flush()

        // Test input
        input = bf.createInput(ByteArrayInputStream(outStream.toByteArray()), 4096)
        val object2 = configuration.readClassAndObject(input)
        println(object2)
        doAssertEquals(object1, object2)
    }

    fun <T : Any> roundTripClass(type: KClass<T>, bf: BufferFactory = DefaultBufferFactory()) {
        val outStream = ByteArrayOutputStream();
        // Test output
        output = bf.createOutput(outStream, 4096)
        configuration.writeClass(output, type)
        output.flush()

        // Test input
        input = bf.createInput(ByteArrayInputStream(outStream.toByteArray()), 4096)
        val object2 = configuration.readClass(input)!!.type
        doAssertEquals(type, object2)
    }
    fun <T> doAssertEquals(object1: T, object2: T) {
        assertEquals(object1, object2)
    }
}