package org.softpark.stateful4k.config.events

public interface IEventConfigurator<C, S, E, AS: S, AE: E> {
    fun alias(name: String): IEventConfigurator<C, S, E, AS, AE>
    fun filter(predicate: IEventContext<C, AS, AE>.() -> Boolean): IEventConfigurator<C, S, E, AS, AE>
    fun trigger(action: IEventContext<C, AS, AE>.() -> Unit): IEventConfigurator<C, S, E, AS, AE>
    fun goto(resolver: IEventContext<C, AS, AE>.() -> S): IEventConfigurator<C, S, E, AS, AE>
    fun loop(action: IEventContext<C, AS, AE>.() -> Unit): IEventConfigurator<C, S, E, AS, AE>
    fun loop(): IEventConfigurator<C, S, E, AS, AE>
}