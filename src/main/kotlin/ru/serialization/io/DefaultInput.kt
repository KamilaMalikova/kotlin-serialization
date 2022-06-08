package ru.serialization.io

import ru.serialization.Configuration
import ru.serialization.exception.DefaultException
import ru.serialization.exception.SerializationException
import java.io.IOException
import java.io.InputStream
import java.lang.Double.longBitsToDouble
import java.lang.Float.intBitsToFloat
import kotlin.experimental.and

open class DefaultInput(
    private val bufferSize: Int = 4096,
    private val varEncoding: Boolean = true,
    var inputStream: InputStream? = null
): Input() {
    var position = 0
    private var capacity = 4096
    var limit = 0
    var total: Long = 0
    var chars = CharArray(32)
    var buffer: ByteArray = ByteArray(bufferSize)

    override fun readByte(): Byte {
        if (position == limit) require(1)
        return buffer[position++]
    }

    override fun readBoolean(): Boolean {
        if (position == limit) require(1)
        return buffer[position++].toInt() == 1
    }

    override fun readChar(): Char {
        require(2)
        val p = position
        position = p + 2
        return ((buffer[p].toInt() and 0xFF) or (buffer[p + 1].toInt() and 0xFF shl 8)).toChar()
    }

    override fun readShort(): Short {
        require(2)
        val p = position
        position = p + 2
        return ((buffer[p].toInt() and 0xFF) or (buffer[p + 1].toInt() and 0xFF shl 8)).toShort()
    }

    override fun readInt(optimizePositive: Boolean): Int {
        if (varEncoding) return readVarInt(optimizePositive)
        require(4)
        val p = position
        position = p + 4
        return (buffer[p].toInt() and 0xFF) or (buffer[p + 1].toInt() and 0xFF shl 8) or (buffer[p + 2].toInt() and 0xFF shl 16)
    }

    override fun readLong(): Long {
        TODO("Not yet implemented")
    }

    override fun readString(): String? {
        if (!readVarIntFlag()) return readAsciiString() // ASCII.

        // Null, empty, or UTF8.
        var charCount: Int = readVarIntFlag(true)
        when (charCount) {
            0 -> return null
            1 -> return ""
        }
        readUtf8Chars(charCount--)
        return String(chars, 0, charCount)
    }

    override fun readVarLong(optimizePositive: Boolean): Long {
        if (require(1) < 9) return readVarLongSlow(optimizePositive)
        var p = position
        var b = buffer[p++].toInt()
        var result = (b and 0x7F).toLong()
        if (b and 0x80 != 0) {
            val buffer = buffer
            b = buffer[p++].toInt()
            result = result or (b and 0x7F shl 7).toLong()
            if (b and 0x80 != 0) {
                b = buffer[p++].toInt()
                result = result or (b and 0x7F shl 14).toLong()
                if (b and 0x80 != 0) {
                    b = buffer[p++].toInt()
                    result = result or (b and 0x7F shl 21).toLong()
                    if (b and 0x80 != 0) {
                        b = buffer[p++].toInt()
                        result = result or ((b and 0x7F).toLong() shl 28)
                        if (b and 0x80 != 0) {
                            b = buffer[p++].toInt()
                            result = result or ((b and 0x7F).toLong() shl 35)
                            if (b and 0x80 != 0) {
                                b = buffer[p++].toInt()
                                result = result or ((b and 0x7F).toLong() shl 42)
                                if (b and 0x80 != 0) {
                                    b = buffer[p++].toInt()
                                    result = result or ((b and 0x7F).toLong() shl 49)
                                    if (b and 0x80 != 0) {
                                        b = buffer[p++].toInt()
                                        result = result or (b.toLong() shl 56)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        position = p
        return if (optimizePositive) result else result ushr 1 xor -(result and 1)
    }

    override fun readFloat(): Float {
        require(4)
        val buffer = buffer
        val p = position
        position = p + 4
        return intBitsToFloat(
                (buffer[p].toInt() and 0xFF
                        ) or (buffer[p + 1].toInt() and 0xFF shl 8
                        ) or (buffer[p + 2].toInt() and 0xFF shl 16
                        ) or (buffer[p + 3].toInt() and 0xFF shl 24)
                )
    }

    override fun readDouble(): Double {
        require(8)
        val buffer = buffer
        val p = position
        position = p + 8

        return longBitsToDouble(
            (buffer[p].toLong() and 0xFF
                ) or (buffer[p + 1].toLong() and 0xFF shl 8
                ) or (buffer[p + 2].toLong() and 0xFF shl 16
                ) or ((buffer[p + 3].toLong() and 0xFF) shl 24
                ) or ((buffer[p + 4].toLong() and 0xFF) shl 32
                ) or ((buffer[p + 5].toLong() and 0xFF) shl 40
                ) or ((buffer[p + 6].toLong() and 0xFF) shl 48
                ) or (buffer[p + 7].toLong() shl 56)
        )
    }

    override fun readBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        readBytes(bytes, 0, length)
        return bytes
    }

    /** Reads count bytes and writes them to the specified byte[], starting at offset.  */
    @Throws(SerializationException::class)
    fun readBytes(bytes: ByteArray, offset: Int, count: Int) {
        var offset = offset
        var count = count
        var copyCount = (limit - position).coerceAtMost(count)
        while (true) {
            System.arraycopy(buffer, position, bytes, offset, copyCount)
            position += copyCount
            count -= copyCount
            if (count == 0) break
            offset += copyCount
            copyCount = count.coerceAtMost(capacity)
            require(copyCount)
        }
    }

    /** Reads the boolean part of a varint flag. The position is not advanced, [.readVarIntFlag] should be used to
     * advance the position.  */
    private fun readVarIntFlag(): Boolean {
        if (position == limit) require(1)
        return (buffer[position] and 0x80.toByte()) != 0.toByte()
    }

    private fun readVarIntFlag(optimizePositive: Boolean): Int {
        if (require(1) < 5) return readVarIntFlagSlow(optimizePositive)
        var b = buffer[position++].toInt()
        var result = b and 0x3F // Mask first 6 bits.

        if (b and 0x40 != 0) { // Bit 7 means another byte, bit 8 means UTF8.
            val buffer = buffer
            var p = position
            b = buffer[p++].toInt()
            result = result or (b and 0x7F shl 6)
            if (b and 0x80 != 0) {
                b = buffer[p++].toInt()
                result = result or (b and 0x7F shl 13)
                if (b and 0x80 != 0) {
                    b = buffer[p++].toInt()
                    result = result or (b and 0x7F shl 20)
                    if (b and 0x80 != 0) {
                        b = buffer[p++].toInt()
                        result = result or (b and 0x7F shl 27)
                    }
                }
            }
            position = p
        }
        return if (optimizePositive) result else result ushr 1 xor -(result and 1)
    }

    private fun readVarIntFlagSlow(optimizePositive: Boolean): Int {
        // The buffer is guaranteed to have at least 1 byte.
        var b = buffer[position++].toInt()
        var result = b and 0x3F
        if (b and 0x40 != 0) {
            if (position == limit) require(1)
            val buffer = buffer
            b = buffer[position++].toInt()
            result = result or (b and 0x7F shl 6)
            if (b and 0x80 != 0) {
                if (position == limit) require(1)
                b = buffer[position++].toInt()
                result = result or (b and 0x7F shl 13)
                if (b and 0x80 != 0) {
                    if (position == limit) require(1)
                    b = buffer[position++].toInt()
                    result = result or (b and 0x7F shl 20)
                    if (b and 0x80 != 0) {
                        if (position == limit) require(1)
                        b = buffer[position++].toInt()
                        result = result or (b and 0x7F shl 27)
                    }
                }
            }
        }
        return if (optimizePositive) result else result ushr 1 xor -(result and 1)
    }

    private fun readAsciiString(): String? {
        val chars = chars
        val buffer = buffer
        var p = position
        var charCount = 0
        val n = chars.size.coerceAtMost(limit - position)
        while (charCount < n) {
            val b = buffer[p].toInt()
            if (b and 0x80 == 0x80) {
                position = p + 1
                chars[charCount] = (b and 0x7F).toChar()
                return String(chars, 0, charCount + 1)
            }
            chars[charCount] = b.toChar()
            charCount++
            p++
        }
        position = p
        return readAsciiSlow(charCount)
    }

    private fun readAsciiSlow(charCount: Int): String {
        var charCount = charCount
        var chars = chars
        val buffer = buffer
        while (true) {
            if (position == limit) require(1)
            val b = buffer[position++].toInt()
            if (charCount == chars.size) {
                val newChars = CharArray(charCount * 2)
                System.arraycopy(chars, 0, newChars, 0, charCount)
                chars = newChars
                this.chars = newChars
            }
            if (b and 0x80 == 0x80) {
                chars[charCount] = (b and 0x7F).toChar()
                return String(chars, 0, charCount + 1)
            }
            chars[charCount++] = b.toChar()
        }
    }

    private fun readUtf8Chars(charCount: Int) {
        if (chars.size < charCount) chars = CharArray(charCount)
        val buffer = buffer
        val chars = chars
        // Try to read 7 bit ASCII chars.
        var charIndex = 0
        val count = require(1).coerceAtMost(charCount)
        var p = position
        var b: Int
        while (charIndex < count) {
            b = buffer[p++].toInt()
            if (b < 0) {
                p--
                break
            }
            chars[charIndex++] = b.toChar()
        }
        position = p
        // If buffer didn't hold all chars or any were not ASCII, use slow path for remainder.
        if (charIndex < charCount) readUtf8CharsSlow(charCount, charIndex)
    }

    private fun readUtf8CharsSlow(charCount: Int, charIndex: Int) {
        var charIndex = charIndex
        val chars = chars
        val buffer = buffer
        while (charIndex < charCount) {
            if (position == limit) require(1)
            val b = (buffer[position++] and 0xFF.toByte()).toInt()
            when (b shr 4) {
                0, 1, 2, 3, 4, 5, 6, 7 -> chars[charIndex] = b.toChar()
                12, 13 -> {
                    if (position == limit) require(1)
                    chars[charIndex] = (b and 0x1F shl 6 or buffer[position++].toInt() and 0x3F).toChar()
                }
                14 -> {
                    require(2)
                    val p = position
                    position = p + 2
                    chars[charIndex] =
                        (b and 0x0F shl 12 or (buffer[p].toInt() and 0x3F shl 6) or (buffer[p + 1] and 0x3F).toInt()).toChar()
                }
            }
            charIndex++
        }
    }

    override fun readVarInt(optimizePositive: Boolean): Int {
        if (require(1) < 5) return readVarIntSlow(optimizePositive)
        var b = buffer[position++].toInt()
        var result = b and 0x7F
        if (b and 0x80 != 0) {
            val buffer = buffer
            var p = position
            b = buffer[p++].toInt()
            result = result or (b and 0x7F shl 7)
            if (b and 0x80 != 0) {
                b = buffer[p++].toInt()
                result = result or (b and 0x7F shl 14)
                if (b and 0x80 != 0) {
                    b = buffer[p++].toInt()
                    result = result or (b and 0x7F shl 21)
                    if (b and 0x80 != 0) {
                        b = buffer[p++].toInt()
                        result = result or (b and 0x7F shl 28)
                    }
                }
            }
            position = p
        }
        return if (optimizePositive) result else result ushr 1 xor -(result and 1)
    }

    private fun readVarIntSlow(optimizePositive: Boolean): Int {
        // The buffer is guaranteed to have at least 1 byte.
        var b = buffer[position++].toInt()
        var result = b and 0x7F
        if (b and 0x80 != 0) {
            if (position == limit) require(1)
            val buffer = buffer
            b = buffer[position++].toInt()
            result = result or (b and 0x7F shl 7)
            if (b and 0x80 != 0) {
                if (position == limit) require(1)
                b = buffer[position++].toInt()
                result = result or (b and 0x7F shl 14)
                if (b and 0x80 != 0) {
                    if (position == limit) require(1)
                    b = buffer[position++].toInt()
                    result = result or (b and 0x7F shl 21)
                    if (b and 0x80 != 0) {
                        if (position == limit) require(1)
                        b = buffer[position++].toInt()
                        result = result or (b and 0x7F shl 28)
                    }
                }
            }
        }
        return if (optimizePositive) result else result ushr 1 xor -(result and 1)
    }

    private fun readVarLongSlow(optimizePositive: Boolean): Long {
        // The buffer is guaranteed to have at least 1 byte.
        var b = buffer[position++].toInt()
        var result = (b and 0x7F).toLong()
        if (b and 0x80 != 0) {
            if (position == limit) require(1)
            val buffer = buffer
            b = buffer[position++].toInt()
            result = result or (b and 0x7F shl 7).toLong()
            if (b and 0x80 != 0) {
                if (position == limit) require(1)
                b = buffer[position++].toInt()
                result = result or (b and 0x7F shl 14).toLong()
                if (b and 0x80 != 0) {
                    if (position == limit) require(1)
                    b = buffer[position++].toInt()
                    result = result or (b and 0x7F shl 21).toLong()
                    if (b and 0x80 != 0) {
                        if (position == limit) require(1)
                        b = buffer[position++].toInt()
                        result = result or ((b and 0x7F).toLong() shl 28)
                        if (b and 0x80 != 0) {
                            if (position == limit) require(1)
                            b = buffer[position++].toInt()
                            result = result or ((b and 0x7F).toLong() shl 35)
                            if (b and 0x80 != 0) {
                                if (position == limit) require(1)
                                b = buffer[position++].toInt()
                                result = result or ((b and 0x7F).toLong() shl 42)
                                if (b and 0x80 != 0) {
                                    if (position == limit) require(1)
                                    b = buffer[position++].toInt()
                                    result = result or ((b and 0x7F).toLong() shl 49)
                                    if (b and 0x80 != 0) {
                                        if (position == limit) require(1)
                                        b = buffer[position++].toInt()
                                        result = result or (b.toLong() shl 56)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return if (optimizePositive) result else result ushr 1 xor -(result and 1)
    }
    /** Fills the buffer with at least the number of bytes specified.
     * @param required Must be > 0.
     * @return The number of bytes remaining in the buffer, which will be at least `required` bytes.
     * @throws SerializationException if [.fill] is unable to provide more bytes (buffer
     * underflow).
     */
    @Throws(SerializationException::class)
    private fun require(required: Int): Int {
        var remaining = limit - position
        if (remaining >= required) return remaining
        if (required > capacity) throw DefaultException("Buffer too small: capacity: $capacity, required: $required")
        var count: Int
        // Try to fill the buffer.
        if (remaining > 0) {
            count = fill(buffer, limit, capacity - limit)
            if (count == -1) throw DefaultException("Buffer underflow.")
            remaining += count
            if (remaining >= required) {
                limit += count
                return remaining
            }
        }

        // Was not enough, compact and try again.
        System.arraycopy(buffer, position, buffer, 0, remaining)
        total += position.toLong()
        position = 0
        while (true) {
            count = fill(buffer, remaining, capacity - remaining)
            if (count == -1) {
                if (remaining >= required) break
                throw DefaultException("Buffer underflow.")
            }
            remaining += count
            if (remaining >= required) break // Enough has been read.
        }
        limit = remaining
        return remaining
    }

    /** Fills the buffer with more bytes. The default implementation reads from the [InputStream][.getInputStream], if set.
     * Can be overridden to fill the bytes from another source.
     * @return -1 if there are no more bytes.
     */
    @Throws(SerializationException::class)
    fun fill(buffer: ByteArray, offset: Int, count: Int): Int {
        return try {
            inputStream!!.read(buffer, offset, count)
        } catch (ex: IOException) {
            throw DefaultException(null, ex)
        }
    }
}