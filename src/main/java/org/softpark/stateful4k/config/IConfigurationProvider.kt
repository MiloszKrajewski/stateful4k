package org.softpark.stateful4k.config

import org.softpark.stateful4k.config.events.IEventConfiguration
import org.softpark.stateful4k.config.states.IStateConfiguration

public interface IConfigurationProvider<C, S, E> {
    val states: Iterable<IStateConfiguration<C, S, E>>
    val events: Iterable<IEventConfiguration<C, S, E>>
}