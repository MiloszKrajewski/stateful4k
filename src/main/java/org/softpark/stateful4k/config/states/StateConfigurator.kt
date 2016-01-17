package org.softpark.stateful4k.config.states

import org.softpark.stateful4k.config.events.EventConfiguration
import org.softpark.stateful4k.config.events.EventConfigurator
import org.softpark.stateful4k.config.events.IEventConfiguration
import org.softpark.stateful4k.config.events.IEventConfigurator

internal class StateConfigurator<C, S, E, AS : S>(
        private val stateType: Class<AS>,
        private val configuration: IStateConfiguration<C, S, E>,
        private val addEvent: (IEventConfiguration<C, S, E>) -> Unit) :
        IStateConfigurator<C, S, E, AS> {

    private val textId: String =
            "State(${configuration.stateType.javaClass.name})"

    override fun alias(name: String): IStateConfigurator<C, S, E, AS> {
        if (configuration.alias != null)
            throw UnsupportedOperationException("$textId.alias(...) has been already defined")
        configuration.alias = name
        return this
    }

    override fun enter(action: IStateContext<C, AS>.() -> Unit): IStateConfigurator<C, S, E, AS> {
        if (configuration.onEnter != null)
            throw UnsupportedOperationException("$textId.enter(...) has been already defined")
        configuration.onEnter = { c, s -> StateContext(c, stateType.cast(s)).action() }
        return this
    }

    override fun exit(action: IStateContext<C, AS>.() -> Unit): IStateConfigurator<C, S, E, AS> {
        if (configuration.onExit != null)
            throw UnsupportedOperationException("$textId.exit(...) has been already defined")
        configuration.onExit = { c, s -> StateContext(c, stateType.cast(s)).action() }
        return this
    }

    override fun <AE : E> event(eventType: Class<AE>): IEventConfigurator<C, S, E, AS, AE> {
        val eventConfig = EventConfiguration<C, S, E, AS, AE>(stateType, eventType).apply(addEvent)
        return EventConfigurator(stateType, eventType, eventConfig)
    }

    override fun toString(): String = "EventConfigurator($configuration)"
}