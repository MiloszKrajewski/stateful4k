package org.softpark.stateful4k.config.states

internal class StateConfiguration<C, S, E, AS: S>(
        override val stateType: Class<AS>):
        IStateConfiguration<C, S, E> {
    override var alias: String? = null
    override var onEnter: ((C, S) -> Unit)? = null
    override var onExit: ((C, S) -> Unit)? = null

    constructor(other: StateConfiguration<C, S, E, AS>): this(other.stateType) {
        alias = other.alias
        onEnter = other.onEnter
        onExit = other.onExit
    }

    override fun freeze(): IStateConfiguration<C, S, E> =
            StateConfiguration(this)

    override fun toString(): String =
            "StateConfiguration(" +
                    "stateType:${stateType.name},alias:'$alias'," +
                    "enter:${onEnter != null},exit:${onExit != null})"
}

