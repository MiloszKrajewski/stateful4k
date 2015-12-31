package org.softpark.stateful4k

import org.junit.Test
import java.util.*
import kotlin.test.assertTrue

class DoorStateMachineTest {
    val door = DoorStateMachine()

    class History: Emitter {
        val history = ArrayList<String>()

        override fun emit(sound: String) {
            history.add(sound)
        }

        fun matches(vararg sounds: String): Boolean {
            val expected = sounds.toArrayList()
            val actual = history
            if (expected.size != actual.size)
                return false
            return expected.zip(actual)
                    .all { val (x, y) = it; x.equals(y) }
        }

        override fun toString(): String {
            return "History(${history.toString()})"
        }
    }

    fun assertTransition(
            initial: DoorState,
            event: DoorEvent,
            checkState: (DoorState) -> Boolean,
            checkHistory: (History) -> Boolean) {
        val history = History()
        val machine = DoorStateMachine().start(history, initial)
        machine.fire(event)
        assertTrue { checkState(machine.state) }
        assertTrue { checkHistory(history) }
    }

    @Test fun `Cannot open opened doors`() {
        listOf(false, true).forEach { locked ->
            assertTransition(
                    DoorClosed(locked),
                    CloseEvent(),
                    { it is DoorClosed },
                    { it.matches() })
        }
    }

    @Test fun `Cannot close closed doors`() {
        listOf(false, true).forEach { locked ->
            assertTransition(
                    DoorClosed(locked),
                    CloseEvent(),
                    { it is DoorClosed },
                    { it.matches() })
        }
    }

    @Test fun `Cannot open locked doors`() {
        assertTransition(
                DoorClosed(true),
                OpenEvent(),
                { it is DoorClosed && it.locked },
                { it.matches("Click! Click!") })
    }

    @Test fun `Cannot close locked doors`() {
        assertTransition(
                DoorOpened(true),
                CloseEvent(),
                { it is DoorOpened && it.locked },
                { it.matches("Squeak! Bang!") }
        )
    }

    @Test fun `Open unlocked doors`() {
        assertTransition(
                DoorClosed(false),
                OpenEvent(),
                { it is DoorOpened && !it.locked },
                { it.matches("Click! Squeak!") }
        )
    }

    @Test fun `Close unlocked doors`() {
        assertTransition(
                DoorOpened(false),
                CloseEvent(),
                { it is DoorClosed && !it.locked },
                { it.matches("Squeak! Click!") }
        )
    }

    @Test fun `Lock closed doors`() {
        assertTransition(
                DoorClosed(false),
                LockEvent(),
                { it is DoorClosed && it.locked },
                { it.matches("Clack!") }
        )
    }

    @Test fun `Lock opened doors`() {
        assertTransition(
                DoorOpened(false),
                LockEvent(),
                { it is DoorOpened && it.locked },
                { it.matches("Clack!") }
        )
    }

    @Test fun `Unlock closed doors`() {
        assertTransition(
                DoorClosed(true),
                UnlockEvent(),
                { it is DoorClosed && !it.locked },
                { it.matches("Click!") }
        )
    }

    @Test fun `Unlock opened doors`() {
        assertTransition(
                DoorOpened(true),
                UnlockEvent(),
                { it is DoorOpened && !it.locked },
                { it.matches("Click!") }
        )
    }
}