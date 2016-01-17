package org.softpark.stateful4k

import org.softpark.stateful4k.extensions.createExecutor
import org.softpark.stateful4k.extensions.event

class ConsoleEmitter : Emitter {
    override fun sound(sound: String) = println(sound)
}

fun quickDemo() {

    val configurator = StateMachine
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
    val executor = configurator.createExecutor(ConsoleEmitter(), DoorClosed(true))
    executor.fire(UnlockEvent())

}
