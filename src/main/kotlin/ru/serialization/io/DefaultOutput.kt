package ru.serialization.io

import ru.serialization.exception.BufferOverflowException
import ru.serialization.exception.SerializationException
import ru.serialization.utils.isAscii
import java.io.OutputStream
import kotlin.experimental.or

open class DefaultOutput(
    bufferSize: Int = 4096,
    private val varEncoding: Boolean = true,
    var outputStream: OutputStream? = null
): Output() {
    var maxCapacity = 4096
    var total: Long = 0
    var position = 0
    var capacity = bufferSize
    var buffer: ByteArray = ByteArray(bufferSize)

    private fun require(required: Int): Boolean {
        if (capacity - position >= required) return false
        flush()
        if (capacity - position >= required) return true
        if (required > maxCapacity - position) {
            if (required > maxCapacity) throw BufferOverflowException("Buffer overflow. Max capacity: $maxCapacity, required: $required")
            throw BufferOverflowException(
                "Buffer overflow. Available: " + (maxCapacity - position) + ", required: " + required
            )
        }
        if (capacity == 0) capacity = 16
        do {
            capacity = Math.min(capacity * 2, maxCapacity)
        } while (capacity - position < required)
        val newBuffer = ByteArray(capacity)
        System.arraycopy(buffer, 0, newBuffer, 0, position)
        buffer = newBuffer
        return true
    }

    override fun writeNull() {
        writeByte(NULL)
    }
    override fun writeByte(serializableValue: Byte) {
        if (position == capacity) require(1)
        buffer[position++] = serializableValue
    }

    override fun writeBoolean(serializableValue: Boolean) {
        if (position == capacity) require(1)
        buffer[position++] = if (serializableValue) 1.toByte() else 0
    }

    override fun writeChar(serializableValue: Char) {
        require(2)
        val p = position
        position = p + 2
        buffer[p] = serializableValue.code.toByte()
        buffer[p + 1] = (serializableValue.code ushr 8).toByte()
    }

    override fun writeShort(serializableValue: Short) {
        require(2)
        val p = position
        position = p + 2
        buffer[p] = serializableValue.toByte()
        buffer[p + 1] = (serializableValue.toInt() ushr 8).toByte()
    }

    override fun writeInt(serializableValue: Int, optimizePositive: Boolean): Int {
        if (varEncoding) return writeVarInt(serializableValue, optimizePositive)
        require(4)
        val buffer = buffer
        val p = position
        position = p + 4
        buffer[p] = serializableValue.toByte()
        buffer[p + 1] = (serializableValue shr 8).toByte()
        buffer[p + 2] = (serializableValue shr 16).toByte()
        buffer[p + 3] = (serializableValue shr 24).toByte()
        return 4
    }

    override fun writeLong(serializableValue: Long) {
        TODO("Not yet implemented")
    }

    override fun writeString(value: String?) {
        if (value == null) {
            writeByte(0x80.toByte()) // 0 means null, bit 8 means UTF8.
            return
        }
        val charCount: Int = value.length
        if (charCount == 0) {
            writeByte((1 or 0x80).toByte()) // 1 means empty string, bit 8 means UTF8.
            return
        }

        // Detect ASCII.
        if (charCount in 2..32 && value.isAscii()) {
            if (capacity - position < charCount) writeAscii_slow(value, charCount) else {
                value.toByteArray()
                    .copyInto(buffer, position, 0, charCount)
                position += charCount
            }
            buffer[position - 1] = buffer[position - 1] or 0x80.toByte()
            return
        }
        writeVarIntFlag(true, charCount + 1, true)
        var charIndex = 0
        if (capacity - position >= charCount) {
            // Try to write 7 bit chars.
            var p = position
            while (true) {
                val c: Int = value[charIndex].code
                if (c > 127) break
                buffer[p++] = c.toByte()
                charIndex++
                if (charIndex == charCount) {
                    position = p
                    return
                }
            }
            position = p
        }
        if (charIndex < charCount) writeUtf8Slow(value, charCount, charIndex)
    }

    private fun writeAscii_slow(value: String, charCount: Int) {
        if (charCount == 0) return
        if (position == capacity) require(1) // Must be able to write at least one character.

        var charIndex = 0
        var buffer = buffer
        var charsToWrite = Math.min(charCount, capacity - position)
        while (charIndex < charCount) {
            value.toByteArray()
                .copyInto(buffer, position, charIndex, charIndex+charsToWrite)
            charIndex += charsToWrite
            position += charsToWrite
            charsToWrite = (charCount - charIndex).coerceAtMost(capacity)
            if (require(charsToWrite)) buffer = this.buffer
        }
    }

    private fun writeUtf8Slow(value: String, charCount: Int, charIndex: Int) {
        var index = charIndex
        while (index < charCount) {
            if (position == capacity) require(capacity.coerceAtMost(charCount - index))
            val c = value[index].code
            if (c <= 0x007F) buffer[position++] = c.toByte() else if (c > 0x07FF) {
                buffer[position++] = (0xE0 or c shr 12 and 0x0F).toByte()
                require(2)
                buffer[position++] = (0x80 or c shr 6 and 0x3F).toByte()
                buffer[position++] = (0x80 or c and 0x3F).toByte()
            } else {
                buffer[position++] = (0xC0 or c shr 6 and 0x1F).toByte()
                if (position == capacity) require(1)
                buffer[position++] = (0x80 or c and 0x3F).toByte()
            }
            index++
        }
    }

    /** Writes a 1-5 byte int.
     * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     *           inefficient (5 bytes).
     * @return The number of bytes written.
     * @see #varIntLength(int, boolean) */
    override fun writeVarInt(value: Int, optimizePositive: Boolean): Int {
        var value2 = value
        if (!optimizePositive) value2 = value2 shl 1 xor (value2 shr 31)
        if (value2 ushr 7 == 0) {
            if (position == capacity) require(1)
            buffer[position++] = value2.toByte()
            return 1
        }
        if (value2 ushr 14 == 0) {
            require(2)
            val p = position
            position = p + 2
            buffer[p] = (value2 and 0x7F or 0x80).toByte()
            buffer[p + 1] = (value2 ushr 7).toByte()
            return 2
        }
        if (value2 ushr 21 == 0) {
            require(3)
            val p = position
            position = p + 3
            val buffer = buffer
            buffer[p] = (value2 and 0x7F or 0x80).toByte()
            buffer[p + 1] = (value2 ushr 7 or 0x80).toByte()
            buffer[p + 2] = (value2 ushr 14).toByte()
            return 3
        }
        if (value2 ushr 28 == 0) {
            require(4)
            val p = position
            position = p + 4
            val buffer = buffer
            buffer[p] = (value2 and 0x7F or 0x80).toByte()
            buffer[p + 1] = (value2 ushr 7 or 0x80).toByte()
            buffer[p + 2] = (value2 ushr 14 or 0x80).toByte()
            buffer[p + 3] = (value2 ushr 21).toByte()
            return 4
        }
        require(5)
        val p = position
        position = p + 5
        val buffer = buffer
        buffer[p] = (value2 and 0x7F or 0x80).toByte()
        buffer[p + 1] = (value2 ushr 7 or 0x80).toByte()
        buffer[p + 2] = (value2 ushr 14 or 0x80).toByte()
        buffer[p + 3] = (value2 ushr 21 or 0x80).toByte()
        buffer[p + 4] = (value2 ushr 28).toByte()
        return 5
    }

    /** Writes a 1-5 byte int, encoding the boolean value with a bit flag.
     * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     *           inefficient (5 bytes).
     * @return The number of bytes written. */
    override fun writeVarIntFlag(flag: Boolean, value: Int, optimizePositive: Boolean): Int {
        var value2 = value
        if (!optimizePositive) value2 = value2 shl 1 xor (value2 shr 31)
        val first = value2 and 0x3F or if (flag) 0x80 else 0 // Mask first 6 bits, bit 8 is the flag.

        if (value2 ushr 6 == 0) {
            if (position == capacity) require(1)
            buffer[position++] = first.toByte()
            return 1
        }
        if (value2 ushr 13 == 0) {
            require(2)
            val p = position
            position = p + 2
            buffer[p] = (first or 0x40).toByte() // Set bit 7.
            buffer[p + 1] = (value2 ushr 6).toByte()
            return 2
        }
        if (value2 ushr 20 == 0) {
            require(3)
            val buffer = buffer
            val p = position
            position = p + 3
            buffer[p] = (first or 0x40).toByte() // Set bit 7.
            buffer[p + 1] = (value2 ushr 6 or 0x80).toByte() // Set bit 8.
            buffer[p + 2] = (value2 ushr 13).toByte()
            return 3
        }
        if (value2 ushr 27 == 0) {
            require(4)
            val buffer = buffer
            val p = position
            position = p + 4
            buffer[p] = (first or 0x40).toByte() // Set bit 7.
            buffer[p + 1] = (value2 ushr 6 or 0x80).toByte() // Set bit 8.
            buffer[p + 2] = (value2 ushr 13 or 0x80).toByte() // Set bit 8.
            buffer[p + 3] = (value2 ushr 20).toByte()
            return 4
        }
        require(5)
        val buffer = buffer
        val p = position
        position = p + 5
        buffer[p] = (first or 0x40).toByte() // Set bit 7.
        buffer[p + 1] = (value2 ushr 6 or 0x80).toByte() // Set bit 8.
        buffer[p + 2] = (value2 ushr 13 or 0x80).toByte() // Set bit 8.
        buffer[p + 3] = (value2 ushr 20 or 0x80).toByte() // Set bit 8.
        buffer[p + 4] = (value2 ushr 27).toByte()
        return 5
    }

    override fun writeVarLong(serializableValue: Long, optimizePositive: Boolean): Int {
        var value2 = serializableValue
        if (!optimizePositive) value2 = value2 shl 1 xor (value2 shr 63)
        if (value2 ushr 7 == 0L) {
            if (position == capacity) require(1)
            buffer[position++] = value2.toByte()
            return 1
        }
        if (value2 ushr 14 == 0L) {
            require(2)
            val p = position
            position = p + 2
            buffer[p] = (value2 and 0x7F or 0x80).toByte()
            buffer[p + 1] = (value2 ushr 7).toByte()
            return 2
        }
        if (value2 ushr 21 == 0L) {
            require(3)
            val p = position
            position = p + 3
            val buffer = buffer
            buffer[p] = (value2 and 0x7F or 0x80).toByte()
            buffer[p + 1] = (value2 ushr 7 or 0x80).toByte()
            buffer[p + 2] = (value2 ushr 14).toByte()
            return 3
        }
        if (value2 ushr 28 == 0L) {
            require(4)
            val p = position
            position = p + 4
            val buffer = buffer
            buffer[p] = (value2 and 0x7F or 0x80).toByte()
            buffer[p + 1] = (value2 ushr 7 or 0x80).toByte()
            buffer[p + 2] = (value2 ushr 14 or 0x80).toByte()
            buffer[p + 3] = (value2 ushr 21).toByte()
            return 4
        }
        if (value2 ushr 35 == 0L) {
            require(5)
            val p = position
            position = p + 5
            val buffer = buffer
            buffer[p] = (value2 and 0x7F or 0x80).toByte()
            buffer[p + 1] = (value2 ushr 7 or 0x80).toByte()
            buffer[p + 2] = (value2 ushr 14 or 0x80).toByte()
            buffer[p + 3] = (value2 ushr 21 or 0x80).toByte()
            buffer[p + 4] = (value2 ushr 28).toByte()
            return 5
        }
        if (value2 ushr 42 == 0L) {
            require(6)
            val p = position
            position = p + 6
            val buffer = buffer
            buffer[p] = (value2 and 0x7F or 0x80).toByte()
            buffer[p + 1] = (value2 ushr 7 or 0x80).toByte()
            buffer[p + 2] = (value2 ushr 14 or 0x80).toByte()
            buffer[p + 3] = (value2 ushr 21 or 0x80).toByte()
            buffer[p + 4] = (value2 ushr 28 or 0x80).toByte()
            buffer[p + 5] = (value2 ushr 35).toByte()
            return 6
        }
        if (value2 ushr 49 == 0L) {
            require(7)
            val p = position
            position = p + 7
            val buffer = buffer
            buffer[p] = (value2 and 0x7F or 0x80).toByte()
            buffer[p + 1] = (value2 ushr 7 or 0x80).toByte()
            buffer[p + 2] = (value2 ushr 14 or 0x80).toByte()
            buffer[p + 3] = (value2 ushr 21 or 0x80).toByte()
            buffer[p + 4] = (value2 ushr 28 or 0x80).toByte()
            buffer[p + 5] = (value2 ushr 35 or 0x80).toByte()
            buffer[p + 6] = (value2 ushr 42).toByte()
            return 7
        }
        if (value2 ushr 56 == 0L) {
            require(8)
            val p = position
            position = p + 8
            val buffer = buffer
            buffer[p] = (value2 and 0x7F or 0x80).toByte()
            buffer[p + 1] = (value2 ushr 7 or 0x80).toByte()
            buffer[p + 2] = (value2 ushr 14 or 0x80).toByte()
            buffer[p + 3] = (value2 ushr 21 or 0x80).toByte()
            buffer[p + 4] = (value2 ushr 28 or 0x80).toByte()
            buffer[p + 5] = (value2 ushr 35 or 0x80).toByte()
            buffer[p + 6] = (value2 ushr 42 or 0x80).toByte()
            buffer[p + 7] = (value2 ushr 49).toByte()
            return 8
        }
        require(9)
        val p = position
        position = p + 9
        val buffer = buffer
        buffer[p] = (value2 and 0x7F or 0x80).toByte()
        buffer[p + 1] = (value2 ushr 7 or 0x80).toByte()
        buffer[p + 2] = (value2 ushr 14 or 0x80).toByte()
        buffer[p + 3] = (value2 ushr 21 or 0x80).toByte()
        buffer[p + 4] = (value2 ushr 28 or 0x80).toByte()
        buffer[p + 5] = (value2 ushr 35 or 0x80).toByte()
        buffer[p + 6] = (value2 ushr 42 or 0x80).toByte()
        buffer[p + 7] = (value2 ushr 49 or 0x80).toByte()
        buffer[p + 8] = (value2 ushr 56).toByte()
        return 9
    }

    override fun writeFloat(serializableValue: Float) {
        require(4)
        val buffer = buffer
        val p = position
        position = p + 4
        val intValue = serializableValue.toBits()

        buffer[p] = intValue.toByte()
        buffer[p + 1] = (intValue shr 8).toByte()
        buffer[p + 2] = (intValue shr 16).toByte()
        buffer[p + 3] = (intValue shr 24).toByte()
    }

    override fun writeDouble(serializableValue: Double) {
        require(8)
        val buffer = buffer
        val p = position
        position = p + 8

        val longValue = serializableValue.toBits()
        buffer[p] = longValue.toByte()
        buffer[p + 1] = (longValue ushr 8).toByte()
        buffer[p + 2] = (longValue ushr 16).toByte()
        buffer[p + 3] = (longValue ushr 24).toByte()
        buffer[p + 4] = (longValue ushr 32).toByte()
        buffer[p + 5] = (longValue ushr 40).toByte()
        buffer[p + 6] = (longValue ushr 48).toByte()
        buffer[p + 7] = (longValue ushr 56).toByte()
    }

    override fun writeBytes(bytes: ByteArray) {
        writeBytes(bytes, 0, bytes.size)
    }

    /** Writes the bytes. Note the number of bytes is not written.  */
    @Throws(SerializationException::class)
    private fun writeBytes(bytes: ByteArray, offset: Int, count: Int) {
        var offset = offset
        var count = count
        var copyCount = (capacity - position).coerceAtMost(count)
        while (true) {
            bytes.copyInto(buffer, position, offset, copyCount)
            position += copyCount
            count -= copyCount
            if (count == 0) return
            offset += copyCount
            copyCount = capacity.coerceAtLeast(1).coerceAtMost(count)
            require(copyCount)
        }
    }

    override fun flush() {
        outputStream!!.write(buffer, 0, position)
        outputStream!!.flush()
        total += position
        position = 0
    }

    private companion object {
        const val NULL: Byte = 0
    }
}