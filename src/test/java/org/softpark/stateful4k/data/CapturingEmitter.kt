package org.softpark.stateful4k.data

import java.util.*

class CapturingEmitter : Emitter {
    val history = ArrayList<String>()

    override fun sound(text: String) {
        history.add(text)
    }

    fun matches(vararg sounds: String): Boolean {
        val expected = sounds.toList()
        val actual = history
        if (expected.size != actual.size)
            return false
        return expected.zip(actual)
                .all { val (x, y) = it; x.equals(y) }
    }

    override fun toString(): String {
        return "History(${history.toString()})"
    }
}