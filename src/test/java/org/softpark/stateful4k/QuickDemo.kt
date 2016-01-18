package org.softpark.stateful4k

import org.softpark.stateful4k.extensions.createExecutor
import org.softpark.stateful4k.extensions.event
import org.softpark.stateful4k.extensions.state

class ConsoleEmitter : Emitter {
    override fun sound(sound: String) = println(sound)
}

fun quickDemo() {

    val configurator = StateMachine
            .createConfigurator<Emitter, DoorState, DoorEvent>()
            .apply {
                state(DoorOpened::class).enter { context.sound("Squeak!") }
                state(DoorClosed::class).enter { context.sound("Bang!") }

                event(DoorState::class, UnlockEvent::class)
                        .filter { state.locked }
                        .loop { context.sound("Click!"); state.unlock() }
                event(DoorState::class, LockEvent::class)
                        .filter { !state.locked }
                        .loop { context.sound("Clack!"); state.lock() }

                event(DoorClosed::class, OpenEvent::class)
                        .filter { state.locked }
                        .loop { context.sound("Click! Click!") }
                event(DoorClosed::class, OpenEvent::class)
                        .goto { DoorOpened(false) }

                event(DoorOpened::class, CloseEvent::class)
                        .filter { state.locked }
                        .loop { context.sound("Squeak! Bang!") }
                event(DoorOpened::class, CloseEvent::class)
                        .goto { DoorClosed(false) }

                event(DoorState::class, DoorEvent::class).loop()
            }
    val executor = configurator.createExecutor(ConsoleEmitter(), DoorClosed(true))
    executor.fire(UnlockEvent())

}
