package org.softpark.stateful4k.config.states

import org.softpark.stateful4k.config.events.IEventConfigurator

public interface IStateConfigurator<C, S, E, AS: S> {
    fun alias(name: String): IStateConfigurator<C, S, E, AS>
    fun enter(action: IStateContext<C, AS>.() -> Unit): IStateConfigurator<C, S, E, AS>
    fun exit(action: IStateContext<C, AS>.() -> Unit): IStateConfigurator<C, S, E, AS>
    fun <AE: E> event(eventType: Class<AE>): IEventConfigurator<C, S, E, AS, AE>
}