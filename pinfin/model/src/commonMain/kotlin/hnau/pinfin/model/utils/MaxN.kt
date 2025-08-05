package hnau.pinfin.model.utils

import java.util.PriorityQueue


//TODO to common-kotlin
fun <T> Iterable<T>.maxN(
    n: Int,
    comparator: Comparator<in T>,
): List<T> {
    val heap: PriorityQueue<T> = PriorityQueue(n, comparator)
    forEach { item ->
        if (heap.size < n) {
            heap.add(item)
            return@forEach
        }
        if (comparator.compare(item, heap.peek()) <= 0) {
            return@forEach
        }
        heap.poll()
        heap.add(item)
    }
    return heap
        .sortedWith(comparator)
        .reversed()
}

//TODO to common-kotlin
fun <T : Comparable<T>> Iterable<T>.maxN(
    n: Int,
): List<T> = maxN(
    n = n,
    comparator = Comparator { a: T, b: T -> a.compareTo(b) }
)