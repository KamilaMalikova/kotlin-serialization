package ru.serialization.utils

import com.hazelcast.core.HazelcastInstance
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicInteger


private val BASE_ID = Integer.getInteger("serializer.base.type.id", 6000)
private val counterMap = ConcurrentHashMap<HazelcastInstance, IdSequence>()

fun idForType(hz: HazelcastInstance, type: Class<*>): Int =
    getOrCreateSequence(hz).idFor(type)


fun globalId(hz: HazelcastInstance): Int =
    getOrCreateSequence(hz).idFor(hz.javaClass)

fun instanceDestroyed(hz: HazelcastInstance) {
    counterMap.remove(hz)
}

private fun getOrCreateSequence(hs: HazelcastInstance): IdSequence =
    counterMap[hs] ?: IdSequence().let { counterMap.putIfAbsent(hs, it) ?: it }

private class IdSequence {
    val knownTypes: ConcurrentMap<Class<*>, Int> = ConcurrentHashMap()
    val counter = AtomicInteger(BASE_ID)

    fun idFor(type: Class<*>): Int =
        knownTypes.computeIfAbsent(type) {
                ignored: Class<*>? -> counter.incrementAndGet()
        }

}
