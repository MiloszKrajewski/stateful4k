package org.softpark.stateful4k.demo

import org.softpark.stateful4k.StateMachine
import org.softpark.stateful4k.data.ConsoleEmitter
import org.softpark.stateful4k.extensions.createExecutor
import org.softpark.stateful4k.extensions.event
import org.softpark.stateful4k.extensions.state

fun main(args: Array<String>) { MicroDemo().execute() }

class MicroDemo {
    interface DoorState
    class OpenState : DoorState
    class ClosedState : DoorState

    interface DoorEvent
    class OpenEvent : DoorEvent
    class CloseEvent : DoorEvent

    fun execute() {
        var configurator = StateMachine.createConfigurator<ConsoleEmitter, DoorState, DoorEvent>()
        configurator
                .state(ClosedState::class)
                .event(OpenEvent::class)
                .goto { OpenState() }
        configurator
                .event(OpenState::class, CloseEvent::class)
                .goto { ClosedState() }
        configurator
                .event(DoorState::class, DoorEvent::class)
                .loop()
        configurator
                .state(OpenState::class)
                .enter { context.sound("Squeak!") }
        configurator
                .state(ClosedState::class)
                .enter { context.sound("Bang!") }

        var executor = configurator.createExecutor(ConsoleEmitter(), ClosedState())
        executor.fire(OpenEvent())
    }
}