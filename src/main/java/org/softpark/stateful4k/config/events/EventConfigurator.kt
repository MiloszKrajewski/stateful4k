package org.softpark.stateful4k.config.events

internal class EventConfigurator<C, S, E, AS: S, AE: E>(
        private val stateType: Class<AS>,
        private val eventType: Class<AE>,
        private val configuration: EventConfiguration<C, S, E, AS, AE>):
        IEventConfigurator<C, S, E, AS, AE> {

    private val textId: String =
            "Event(${stateType.name}, ${eventType.name})"

    override fun alias(name: String): IEventConfigurator<C, S, E, AS, AE> {
        if (configuration.alias != null)
            throw UnsupportedOperationException("$textId.alias(...) has been already defined")
        configuration.alias = name
        return this
    }

    override fun trigger(action: IEventContext<C, AS, AE>.() -> Unit): IEventConfigurator<C, S, E, AS, AE> {
        if (configuration.onTrigger != null)
            throw UnsupportedOperationException("$textId.trigger(...) has been already defined")
        configuration.onTrigger = { c, s, e -> EventContext(c, stateType.cast(s), eventType.cast(e)).action() }
        return this
    }

    override fun filter(predicate: IEventContext<C, AS, AE>.() -> Boolean): IEventConfigurator<C, S, E, AS, AE> {
        if (configuration.onValidate != null)
            throw UnsupportedOperationException("$textId.filter(...) has been already defined")
        configuration.onValidate = { c, s, e -> EventContext(c, stateType.cast(s), eventType.cast(e)).predicate() }
        return this
    }

    private fun goto(resolver: IEventContext<C, AS, AE>.() -> S, loop: Boolean): IEventConfigurator<C, S, E, AS, AE> {
        if (configuration.onExecute != null)
            throw UnsupportedOperationException("$textId.goto/loop(...) has been already defined")
        configuration.onExecute = { c, s, e -> EventContext(c, stateType.cast(s), eventType.cast(e)).resolver() }
        configuration.isLoop = loop
        return this
    }

    override fun goto(resolver: IEventContext<C, AS, AE>.() -> S): IEventConfigurator<C, S, E, AS, AE> =
            goto(resolver, false)

    override fun loop(action: IEventContext<C, AS, AE>.() -> Unit): IEventConfigurator<C, S, E, AS, AE> =
            goto({ action(); state }, true)

    override fun loop(): IEventConfigurator<C, S, E, AS, AE> =
            goto({ state }, true)

    override fun toString(): String = "EventConfigurator($configuration)"
}