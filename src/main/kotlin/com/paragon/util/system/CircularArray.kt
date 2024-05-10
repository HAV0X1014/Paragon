package com.paragon.util.system

class CircularArray<T : Number> constructor(private val array: Array<T>, filled: Boolean) : Iterable<T> {

    private var arraySize = if (filled) array.size else 0
    private var tail = -1

    companion object {
        inline fun <reified T : Number> create(size: Int, fill: T) = CircularArray(Array(size) { fill }, true)
        inline fun <reified T : Number> create(size: Int) = CircularArray(Array(size) { 0 as T }, false)
    }

    val head: Int
        get() =
            if (arraySize == array.size)
                (tail + 1) % array.size
            else
                0

    val size: Int
        get() = arraySize

    fun add(item: T) {
        tail = (tail + 1) % array.size
        array[tail] = item
        if (arraySize < array.size) arraySize++
    }

    fun reset() {
        this.arraySize = 0
        this.tail = 0
    }

    fun average() = if (arraySize == 0) 0.0f else ((0 until size).sumOf { array[it].toDouble() } / size).toFloat()

    @Suppress("UNCHECKED_CAST")
    private operator fun get(index: Int): T = when {
        arraySize == 0 || index > arraySize || index < 0 -> throw IndexOutOfBoundsException("$index")
        arraySize == array.size -> array[(head + index) % array.size]
        else -> array[index]
    }

    override fun iterator() = object : Iterator<T> {
        private var index = 0
        override fun hasNext(): Boolean = index < size
        override fun next(): T = try {
            get(index++)
        } catch (e: IndexOutOfBoundsException) {
            index -= 1;
            throw NoSuchElementException(e.message)
        }
    }

}