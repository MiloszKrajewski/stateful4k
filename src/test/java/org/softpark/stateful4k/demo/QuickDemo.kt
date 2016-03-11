package org.softpark.stateful4k.demo

import org.softpark.stateful4k.*
import org.softpark.stateful4k.data.ConsoleEmitter
import org.softpark.stateful4k.extensions.createExecutor
import org.softpark.stateful4k.extensions.event
import org.softpark.stateful4k.extensions.state

class QuickDemo {
    fun execute() {
        val configurator = StateMachine
                .createConfigurator<ConsoleEmitter, DoorState, DoorEvent>()
                .apply {
                    state(OpenState::class).enter { context.sound("Squeak!") }
                    state(ClosedState::class).enter { context.sound("Bang!") }

                    event(DoorState::class, UnlockEvent::class)
                            .filter { state.locked }
                            .loop { context.sound("Click!"); state.unlock() }
                    event(DoorState::class, LockEvent::class)
                            .filter { !state.locked }
                            .loop { context.sound("Clack!"); state.lock() }

                    event(ClosedState::class, OpenEvent::class)
                            .filter { state.locked }
                            .loop { context.sound("Click! Click!") }
                    event(ClosedState::class, OpenEvent::class)
                            .goto { OpenState(false) }

                    event(OpenState::class, CloseEvent::class)
                            .filter { state.locked }
                            .loop { context.sound("Squeak! Bang!") }
                    event(OpenState::class, CloseEvent::class)
                            .goto { ClosedState(false) }

                    event(DoorState::class, DoorEvent::class).loop()
                }
        val executor = configurator.createExecutor(ConsoleEmitter(), ClosedState(true))
        executor.fire(UnlockEvent())
    }
}