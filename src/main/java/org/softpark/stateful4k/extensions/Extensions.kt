package org.softpark.stateful4k.extensions

import org.softpark.stateful4k.StateMachine
import org.softpark.stateful4k.config.IConfigurator
import org.softpark.stateful4k.config.events.IEventConfigurator
import org.softpark.stateful4k.config.states.IStateConfigurator
import kotlin.reflect.KClass

internal fun Class<*>.inheritsFrom(baseClass: Class<*>): Boolean {
    return baseClass.isAssignableFrom(this)
}

internal fun Class<*>.distanceFrom(baseClass: Class<*>): Int? {
    if (!this.inheritsFrom(baseClass)) return null

    fun min(x: Int?, y: Int?): Int? =
            if (x == null) y else if (y == null) x else Math.min(x, y)

    fun distance(thisClass: Class<*>?, total: Int): Int? {
        if (thisClass == null) return null
        if (thisClass == baseClass) return total
        if (thisClass == Any::class.java) return null
        val interfaces = if (baseClass.isInterface) thisClass.interfaces else emptyArray<Class<*>>()
        return interfaces.plus(thisClass.superclass).map { distance(it, total + 1) }.reduce { x, y -> min(x, y) }
    }

    return distance(this, 0)
}

internal fun <T> T?.nullify(value: T): T? =
        if (this == null || this.equals(value)) null else this

fun <C, S : Any, E, AS : S> IConfigurator<C, S, E>.state(
        stateType: KClass<AS>): IStateConfigurator<C, S, E, AS> =
        this.state(stateType.java)

fun <C, S : Any, E : Any, AS : S, AE : E> IConfigurator<C, S, E>.event(
        stateType: KClass<AS>, eventType: KClass<AE>): IEventConfigurator<C, S, E, AS, AE> =
        this.event(stateType.java, eventType.java)

fun <C, S : Any, E : Any, AS : S, AE : E> IStateConfigurator<C, S, E, AS>.event(
        eventType: KClass<AE>): IEventConfigurator<C, S, E, AS, AE> =
        this.event(eventType.java)

fun <C, S : Any, E : Any> IConfigurator<C, S, E>.createExecutor(context: C, state: S) =
        StateMachine.createExecutor(this, context, state)