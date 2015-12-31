package org.softpark.stateful4k.config.events

import org.softpark.stateful4k.config.states.IStateContext

public interface IEventContext<C, out AS, out AE> : IStateContext<C, AS> {
    val event: AE
}

