package org.softpark.stateful4k.action

import org.softpark.stateful4k.config.events.IEventConfiguration
import org.softpark.stateful4k.extensions.distanceFrom
import org.softpark.stateful4k.extensions.nullify

internal class EventExecutor<C, S, E>(
        val stateType: Class<out S>,
        val eventType: Class<out E>,
        val configuration: IEventConfiguration<C, S, E>) :
        Comparable<EventExecutor<C, S, E>> {
    private val stateDistance = stateType.distanceFrom(configuration.stateType) ?: Int.MAX_VALUE
    private val eventDistance = eventType.distanceFrom(configuration.eventType) ?: Int.MAX_VALUE

    val isFallback = configuration.onValidate == null
    val isTransition = configuration.onExecute != null || configuration.isLoop
    val isLoop = configuration.isLoop

    fun validate(context: C, state: S, event: E): Boolean =
            configuration.onValidate?.invoke(context, state, event) ?: true

    fun trigger(context: C, state: S, event: E) =
            configuration.onTrigger?.invoke(context, state, event)

    fun execute(context: C, state: S, event: E): S =
            configuration.onExecute!!.invoke(context, state, event)

    override fun compareTo(other: EventExecutor<C, S, E>): Int =
            stateDistance.compareTo(other.stateDistance).nullify(0) ?:
                    eventDistance.compareTo(other.eventDistance)

    override fun toString(): String {
        return "EventExecutor(" +
                "state:${stateType.name}/$stateDistance,event:${eventType.name}/$eventDistance," +
                "$configuration)"
    }
}
