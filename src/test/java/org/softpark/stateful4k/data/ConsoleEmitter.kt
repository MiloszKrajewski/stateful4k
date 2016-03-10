package org.softpark.stateful4k.data

class ConsoleEmitter : Emitter {
    override fun sound(text: String) = println(text)
}
