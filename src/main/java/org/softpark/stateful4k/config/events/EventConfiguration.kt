package org.softpark.stateful4k.config.events

internal class EventConfiguration<C, S, E, AS: S, AE: E>(
        override val stateType: Class<AS>,
        override val eventType: Class<AE>):
        IEventConfiguration<C, S, E> {
    override var alias: String? = null
    override var onValidate: ((C, S, E) -> Boolean)? = null
    override var onTrigger: ((C, S, E) -> Unit)? = null
    override var onExecute: ((C, S, E) -> S)? = null
    override var isLoop: Boolean = false

    constructor(other: EventConfiguration<C, S, E, AS, AE>): this(other.stateType, other.eventType) {
        alias = other.alias
        onValidate = other.onValidate
        onTrigger = other.onTrigger
        onExecute = other.onExecute
        isLoop = other.isLoop
    }

    override fun freeze(): IEventConfiguration<C, S, E> =
            EventConfiguration(this)

    override fun toString() =
            "EventConfiguration(" +
                    "stateType:${stateType.name},eventType:${eventType.name},alias:'$alias'," +
                    "validate:${onValidate != null}," +
                    "trigger:${onTrigger != null}," +
                    "execute:${onExecute != null}," +
                    "isLoop:$isLoop)"
}

