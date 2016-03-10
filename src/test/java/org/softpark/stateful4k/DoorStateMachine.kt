package org.softpark.stateful4k

import org.softpark.stateful4k.action.IExecutor
import org.softpark.stateful4k.data.Emitter
import org.softpark.stateful4k.extensions.createExecutor
import org.softpark.stateful4k.extensions.event

abstract class DoorState(locked: Boolean) {
    var locked: Boolean = locked; get private set

    fun lock() {
        locked = true
    }

    fun unlock() {
        locked = false
    }
}

class DoorClosed(locked: Boolean) : DoorState(locked) {}
class DoorOpened(locked: Boolean) : DoorState(locked) {}

interface DoorEvent
class LockEvent : DoorEvent
class UnlockEvent : DoorEvent
class CloseEvent : DoorEvent
class OpenEvent : DoorEvent

open class DoorStateMachine {
    val config = StateMachine
            .createConfigurator<Emitter, DoorState, DoorEvent>()
            .apply {
                event(DoorState::class, UnlockEvent::class)
                        .filter { state.locked }
                        .loop { state.unlock(); context.sound("Click!") }
                event(DoorState::class, LockEvent::class)
                        .filter { !state.locked }
                        .loop { state.lock(); context.sound("Clack!") }

                event(DoorClosed::class, OpenEvent::class)
                        .filter { state.locked }
                        .loop { context.sound("Click! Click!") }
                event(DoorClosed::class, OpenEvent::class)
                        .goto { context.sound("Click! Squeak!"); DoorOpened(false) }

                event(DoorOpened::class, CloseEvent::class)
                        .filter { state.locked }
                        .loop { context.sound("Squeak! Bang!") }
                event(DoorOpened::class, CloseEvent::class)
                        .goto { context.sound("Squeak! Click!"); DoorClosed(false) }

                event(DoorState::class, DoorEvent::class).loop()
            }

    fun start(emitter: Emitter, state: DoorState? = null)
            : IExecutor<Emitter, DoorState, DoorEvent> =
            config.createExecutor(emitter, state ?: DoorClosed(true))
}