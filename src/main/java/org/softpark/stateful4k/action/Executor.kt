package org.softpark.stateful4k.action

import org.softpark.stateful4k.config.IConfigurationProvider
import org.softpark.stateful4k.extensions.inheritsFrom
import org.softpark.stateful4k.extensions.nullify
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

internal class Executor<C, S: Any, E: Any>(
        configuration: IConfigurationProvider<C, S, E>, context: C, state: S):
        IExecutor<C, S, E> {

    private val stateList = configuration.states.map { it.freeze() }.toList()
    private val eventList = configuration.events.map { it.freeze() }.toList()
    private val stateMap = HashMap<Class<out S>, List<StateExecutor<C, S, E>>>()
    private val eventMap = HashMap<Pair<Class<out S>, Class<out E>>, List<EventExecutor<C, S, E>>>()

    // order of triggering, depends of hierarchy and declaration order
    private val triggerOrder = Comparator<Pair<EventExecutor<C, S, E>, Int>> { c1, c2 ->
        (-c1.first.compareTo(c2.first)).nullify(0) ?: c1.second.compareTo(c2.second)
    }

    // order of transition, depends on hierarchy, allows one fallback case
    private val transitionOrder = Comparator<EventExecutor<C, S, E>> { e1, e2 ->
        e1.compareTo(e2).nullify(0) ?: e1.isFallback.compareTo(e2.isFallback)
    }

    private val _processing = AtomicBoolean(false)
    private var _state = state

    override val context: C = context
    override val state: S get() = _state

    private fun <T> lock(action: () -> T): T {
        if (_processing.getAndSet(true))
            throw UnsupportedOperationException("Event is currently handled")

        try {
            return action()
        } finally {
            _processing.set(false)
        }
    }

    init {
        lock { onEnter() }
    }

    override fun fire(event: E) = lock { onFire(event) }

    private fun onEnter() {
        val stateType = state.javaClass
        val configs = stateMap.getOrPut(stateType) { cacheState(stateType) }
        configs.forEach { it.enter(context, state) }
    }

    private fun onExit() {
        val stateType = state.javaClass
        val configs = stateMap.getOrPut(stateType, { cacheState(stateType) })
        configs.reversed().forEach { it.exit(context, state) }
    }

    private fun onFire(event: E) {
        val stateType = state.javaClass
        val eventType = event.javaClass
        val eventKey = Pair(stateType, eventType)
        val configs = eventMap
                .getOrPut(eventKey) { cacheEvent(stateType, eventType) }
                .filter { it.validate(context, state, event) }
                .toList()
        val transitions = configs
                .filter { it.isTransition }
                .sortedWith(transitionOrder)
                .toList()

        val firstTransition = transitions.getOrNull(0) ?:
                throw UnsupportedOperationException(
                        "Unexpected event '${eventType.name}' in state '${stateType.name}'. No transition defined.")

        val secondTransition = transitions.getOrNull(1)
        if (secondTransition != null && transitionOrder.compare(firstTransition, secondTransition) == 0)
            throw UnsupportedOperationException(
                    "Unexpected event '${eventType.name}' in state '${stateType.name}'. Ambiguous transition defined.")

        configs.forEach { it.trigger(context, state, event) }

        if (firstTransition.isLoop) {
            firstTransition.execute(context, state, event)
        } else {
            onExit()
            _state = firstTransition.execute(context, state, event)
            onEnter()
        }
    }

    private fun cacheState(stateType: Class<out S>): List<StateExecutor<C, S, E>> {
        return stateList
                .filter { stateType.inheritsFrom(it.stateType) }
                .map { StateExecutor(stateType, it) }
                .sorted().reversed().toList()
    }

    private fun cacheEvent(stateType: Class<out S>, eventType: Class<out E>): List<EventExecutor<C, S, E>> {
        return eventList
                .filter { stateType.inheritsFrom(it.stateType) && eventType.inheritsFrom(it.eventType) }
                .mapIndexed { i, c -> EventExecutor(stateType, eventType, c) to i }
                .sortedWith(triggerOrder) // preserves declaration order
                .map { it.first } // but it does not store it
                .toList()
    }
}
