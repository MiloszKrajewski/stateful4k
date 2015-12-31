package org.softpark.stateful4k.config.states

public interface IStateContext<C, out AS> {
    val context: C
    val state: AS
}