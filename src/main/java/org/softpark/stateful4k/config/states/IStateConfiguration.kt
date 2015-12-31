package org.softpark.stateful4k.config.states

public interface IStateConfiguration<C, S, E> {
    val stateType: Class<out S>
    var alias: String?
    var onEnter: ((C, S) -> Unit)?
    var onExit: ((C, S) -> Unit)?
    fun freeze(): IStateConfiguration<C, S, E>
}