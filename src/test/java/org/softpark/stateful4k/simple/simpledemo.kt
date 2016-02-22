package org.softpark.stateful4k.simple

import org.softpark.stateful4k.StateMachine
import org.softpark.stateful4k.extensions.createExecutor
import org.softpark.stateful4k.extensions.event
import org.softpark.stateful4k.extensions.state

interface DoorState
class OpenState : DoorState
class ClosedState : DoorState

interface DoorEvent
class OpenEvent : DoorEvent
class CloseEvent : DoorEvent

class Emitter {
    fun sound(text: String) = println(text)
}

fun main() {
    var configurator = StateMachine.createConfigurator<Emitter, DoorState, DoorEvent>()
    configurator
            .event(ClosedState::class, OpenEvent::class)
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

    var executor = configurator.createExecutor(Emitter(), ClosedState())
    executor.fire(OpenEvent())
}