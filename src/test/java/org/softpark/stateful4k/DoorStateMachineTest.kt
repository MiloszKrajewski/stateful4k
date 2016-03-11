package org.softpark.stateful4k

import org.junit.Test
import org.softpark.stateful4k.data.CapturingEmitter
import kotlin.test.assertTrue

class DoorStateMachineTest {
    val door = DoorStateMachine()

    fun assertTransition(
            initial: DoorState,
            event: DoorEvent,
            checkState: (DoorState) -> Boolean,
            checkHistory: (CapturingEmitter) -> Boolean) {
        val history = CapturingEmitter()
        val machine = DoorStateMachine().start(history, initial)
        machine.fire(event)
        assertTrue { checkState(machine.state) }
        assertTrue { checkHistory(history) }
    }

    @Test fun `Cannot open opened doors`() {
        listOf(false, true).forEach { locked ->
            assertTransition(
                    ClosedState(locked),
                    CloseEvent(),
                    { it is ClosedState },
                    { it.matches() })
        }
    }

    @Test fun `Cannot close closed doors`() {
        listOf(false, true).forEach { locked ->
            assertTransition(
                    ClosedState(locked),
                    CloseEvent(),
                    { it is ClosedState },
                    { it.matches() })
        }
    }

    @Test fun `Cannot open locked doors`() {
        assertTransition(
                ClosedState(true),
                OpenEvent(),
                { it is ClosedState && it.locked },
                { it.matches("Click! Click!") })
    }

    @Test fun `Cannot close locked doors`() {
        assertTransition(
                OpenState(true),
                CloseEvent(),
                { it is OpenState && it.locked },
                { it.matches("Squeak! Bang!") }
        )
    }

    @Test fun `Open unlocked doors`() {
        assertTransition(
                ClosedState(false),
                OpenEvent(),
                { it is OpenState && !it.locked },
                { it.matches("Click! Squeak!") }
        )
    }

    @Test fun `Close unlocked doors`() {
        assertTransition(
                OpenState(false),
                CloseEvent(),
                { it is ClosedState && !it.locked },
                { it.matches("Squeak! Click!") }
        )
    }

    @Test fun `Lock closed doors`() {
        assertTransition(
                ClosedState(false),
                LockEvent(),
                { it is ClosedState && it.locked },
                { it.matches("Clack!") }
        )
    }

    @Test fun `Lock opened doors`() {
        assertTransition(
                OpenState(false),
                LockEvent(),
                { it is OpenState && it.locked },
                { it.matches("Clack!") }
        )
    }

    @Test fun `Unlock closed doors`() {
        assertTransition(
                ClosedState(true),
                UnlockEvent(),
                { it is ClosedState && !it.locked },
                { it.matches("Click!") }
        )
    }

    @Test fun `Unlock opened doors`() {
        assertTransition(
                OpenState(true),
                UnlockEvent(),
                { it is OpenState && !it.locked },
                { it.matches("Click!") }
        )
    }
}