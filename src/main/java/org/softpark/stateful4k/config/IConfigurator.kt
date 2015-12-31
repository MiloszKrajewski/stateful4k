package org.softpark.stateful4k.config

import org.softpark.stateful4k.config.events.IEventConfigurator
import org.softpark.stateful4k.config.states.IStateConfigurator

public interface IConfigurator<C, S, E>: IConfigurationProvider<C, S, E> {
    fun <AS: S> state(stateType: Class<AS>): IStateConfigurator<C, S, E, AS>
    fun <AS: S, AE: E> event(stateType: Class<AS>, eventType: Class<AE>): IEventConfigurator<C, S, E, AS, AE>
}
