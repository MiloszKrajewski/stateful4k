package org.softpark.stateful4k.config.events

public interface IEventConfiguration<C, S, E> {
    val stateType: Class<out S>
    val eventType: Class<out E>
    val alias: String?
    val onValidate: ((C, S, E) -> Boolean)?
    val onTrigger: ((C, S, E) -> Unit)?
    val onExecute: ((C, S, E) -> S)?
    val isLoop: Boolean
    fun freeze(): IEventConfiguration<C, S, E>
}