package org.softpark.stateful4k.config

import org.softpark.stateful4k.config.events.IEventConfiguration
import org.softpark.stateful4k.config.events.IEventConfigurator
import org.softpark.stateful4k.config.states.IStateConfiguration
import org.softpark.stateful4k.config.states.IStateConfigurator
import org.softpark.stateful4k.config.states.StateConfiguration
import org.softpark.stateful4k.config.states.StateConfigurator
import java.util.*

internal class Configurator<C, S, E>: IConfigurator<C, S, E> {
    private val stateMap = HashMap<Class<out S>, IStateConfiguration<C, S, E>>()
    private val eventMap = HashMap<Pair<Class<out S>, Class<out E>>, MutableList<IEventConfiguration<C, S, E>>>()

    override fun <AS: S> state(stateType: Class<AS>): IStateConfigurator<C, S, E, AS> {
        var stateConfig = stateMap.getOrPut(stateType, { StateConfiguration<C, S, E, AS>(stateType) })
        return StateConfigurator(
                stateType, stateConfig,
                { eventConfig ->
                    val key = Pair(stateType, eventConfig.eventType)
                    val list = eventMap.getOrPut(key, { ArrayList<IEventConfiguration<C, S, E>>() })
                    list.add(eventConfig)
                })
    }

    override fun <AS: S, AE: E> event(stateType: Class<AS>, eventType: Class<AE>): IEventConfigurator<C, S, E, AS, AE> {
        return state(stateType).event(eventType)
    }

    override val states: Iterable<IStateConfiguration<C, S, E>>
        get() = stateMap.values

    override val events: Iterable<IEventConfiguration<C, S, E>>
        get() = eventMap.values.flatMap { it }
}
