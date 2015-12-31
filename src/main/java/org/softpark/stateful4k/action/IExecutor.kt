package org.softpark.stateful4k.action

public interface IExecutor<C, S, E> {
    val context: C
    val state: S
    fun fire(event: E)
}