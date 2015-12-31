package org.softpark.stateful4k

import org.softpark.stateful4k.extensions.*
import org.softpark.stateful4k.StateMachine
import org.softpark.stateful4k.action.IExecutor
import org.softpark.stateful4k.config.IConfigurator
import org.softpark.stateful4k.extensions.createExecutor

interface Emitter {
    fun emit(sound: String)
}

open class DoorState(locked: Boolean) {
    private var _locked = locked
    val locked: Boolean get() = _locked

    fun lock() {
        _locked = true
    }

    fun unlock() {
        _locked = false
    }
}

class DoorClosed(locked: Boolean): DoorState(locked) {}
class DoorOpened(locked: Boolean): DoorState(locked) {}

interface DoorEvent
class LockEvent: DoorEvent
class UnlockEvent: DoorEvent
class CloseEvent: DoorEvent
class OpenEvent: DoorEvent

open class DoorStateMachine {
    val config = StateMachine
            .createConfigurator<Emitter, DoorState, DoorEvent>()
            .apply {
                event(DoorState::class, UnlockEvent::class)
                        .filter { it.state.locked }
                        .loop { it.state.unlock(); it.context.emit("Click!") }
                event(DoorState::class, LockEvent::class)
                        .filter { !it.state.locked }
                        .loop { it.state.lock(); it.context.emit("Clack!") }

                event(DoorClosed::class, OpenEvent::class)
                        .filter { it.state.locked }
                        .loop { it.context.emit("Click! Click!") }
                event(DoorClosed::class, OpenEvent::class)
                        .goto { it.context.emit("Click! Squeak!"); DoorOpened(false) }

                event(DoorOpened::class, CloseEvent::class)
                        .filter { it.state.locked }
                        .loop { it.context.emit("Squeak! Bang!") }
                event(DoorOpened::class, CloseEvent::class)
                        .goto { it.context.emit("Squeak! Click!"); DoorClosed(false) }

                event(DoorState::class, DoorEvent::class).loop()
            }

    fun start(emitter: Emitter, state: DoorState? = null)
            : IExecutor<Emitter, DoorState, DoorEvent> =
            config.createExecutor(emitter, state ?: DoorClosed(true))
}