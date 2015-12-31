package org.softpark.stateful4k.action

import org.softpark.stateful4k.config.states.IStateConfiguration
import org.softpark.stateful4k.extensions.distanceFrom

internal class StateExecutor<C, S, E>(
        val stateType: Class<out S>,
        val configuration: IStateConfiguration<C, S, E>):
        Comparable<StateExecutor<C, S, E>> {
    val distance = stateType.distanceFrom(configuration.stateType) ?: Int.MAX_VALUE

    fun enter(context: C, state: S) =
            configuration.onEnter?.invoke(context, state)

    fun exit(context: C, state: S) =
            configuration.onExit?.invoke(context, state)

    override fun compareTo(other: StateExecutor<C, S, E>): Int =
            distance.compareTo(other.distance)

    override fun toString(): String =
            "StateExecutor(type:${stateType.name},distance:$distance,$configuration)"
}
