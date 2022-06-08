package ru.serialization.utils

class IntMap<V>: Iterable<IntMap.Entry<V>> {
    var size: Int = 0
    lateinit var keyTable: Array<Int>

    override fun iterator(): Iterator<Entry<V>> {
        TODO("Not yet implemented")
    }

    data class Entry<V>(val key: Int, val value: V) {
        override fun toString(): String = "$key = $value"
    }
}