package org.softpark.stateful4k

import org.junit.Test
import org.softpark.stateful4k.StateMachine
import org.softpark.stateful4k.extensions.createExecutor
import org.softpark.stateful4k.extensions.event
import org.softpark.stateful4k.extensions.state
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class StateMachineTest {
    private val _visited = StringBuilder()
    val visited: String get() = _visited.toString()

    fun visit(s: String) = _visited.append(s)
    fun assertVisited(s: String) = assertEquals(s, visited)

    private val cfg = StateMachine.createConfigurator<Context, State, Event>()

    @Test fun `Can configure state`() {
        cfg.state(StateA::class)
                .alias("StateA")
                .enter { println("Hello from StateA") }
                .exit { println("StateA says goodbye") }
    }

    @Test fun `Cannot reconfigure state alias`() {
        assertFailsWith(UnsupportedOperationException::class) {
            cfg.state(StateA::class)
                    .alias("x")
                    .alias("y")
        }
    }

    @Test fun `Cannot reconfigure state enter`() {
        assertFailsWith(UnsupportedOperationException::class) {
            cfg.state(StateA::class)
                    .enter { println("once") }
                    .enter { println("twice") }
        }
    }

    @Test fun `Cannot reconfigure state exit`() {
        assertFailsWith(UnsupportedOperationException::class) {
            cfg.state(StateA::class)
                    .exit { println("once") }
                    .exit { println("twice") }
        }
    }

    @Test fun `Accessing same state returns same state`() {
        val x = cfg.state(StateA::class)
        val y = cfg.state(StateA::class)
        x.alias("x")
        assertFailsWith(UnsupportedOperationException::class) { y.alias("y") }
    }

    @Test fun `Accessing same event creates new entry`() {
        val x = cfg.event(StateA::class, EventA::class)
        val y = cfg.event(StateA::class, EventA::class)
        x.alias("x")
        y.alias("y") // does not throw (yet!)
    }

    @Test fun `Can chain state and event`() {
        val s = cfg.state(StateA::class)
        val e = s.event(EventA::class)
        s.enter { println("enter") }
        e.trigger { println("trigger") }
    }

    @Test fun `Triggers enter on initial state`() {
        cfg.state(StateA::class).enter({ visit("A") })
        assertVisited("")
        /* val exe = */cfg.createExecutor(Context(), StateA())
        assertVisited("A")
    }

    @Test fun `Does not trigger enter of non-visited state`() {
        cfg.state(StateB::class).enter({ visit("eB") })
        assertVisited("")
        val exe = cfg.createExecutor(Context(), StateA())
        assertVisited("")
        assertTrue { exe.state.javaClass == StateA::class.java }
    }

    @Test fun `Triggers exit on exit`() {
        cfg.state(StateA::class).enter({ visit("Ea") }).exit({ visit("Xa") })
        cfg.state(StateB::class).enter({ visit("Eb") })
        cfg.event(StateA::class, EventA::class).goto { visit("Gab"); StateB() }
        assertVisited("")
        val exe = cfg.createExecutor(Context(), StateA())
        assertVisited("Ea")
        exe.fire(EventA())
        assertVisited("EaXaGabEb")
        assertTrue { exe.state.javaClass == StateB::class.java }
    }

    @Test fun `Enter is triggered bottom-up`() {
        cfg.state(State::class).enter({ visit("0") })
        cfg.state(StateA::class).enter({ visit("A") })
        cfg.event(StateB::class, EventA::class).goto { visit("G"); StateA() }
        val exe = cfg.createExecutor(Context(), StateB())
        assertVisited("0")
        exe.fire(EventA())
        assertVisited("0G0A")
    }

    @Test fun `Exit is triggered top-down`() {
        cfg.state(State::class).exit({ visit("0") })
        cfg.state(StateA::class).exit({ visit("A") })
        cfg.event(StateA::class, Event::class).goto { visit("Ga"); StateB() }
        cfg.event(StateB::class, Event::class).goto { visit("Gb"); StateA() }
        val exe = cfg.createExecutor(Context(), StateA())
        assertVisited("")
        exe.fire(Event()) // StateA -> StateB
        assertVisited("A0Ga")
    }

    @Test fun `Triggers are triggered when pattern matches`() {
        cfg.state(State::class)
        cfg.state(StateA::class)
        cfg.event(State::class, Event::class).trigger { visit("0") }
        cfg.event(StateA::class, EventA::class).goto { StateB() }
        cfg.event(StateB::class, EventB::class).goto { StateA() }
        val exe = cfg.createExecutor(Context(), StateA())
        assertVisited("")
        exe.fire(EventA())
        assertVisited("0")
    }

    @Test fun `Triggers are not triggered when filtered out`() {
        cfg.state(State::class)
        cfg.state(StateA::class)
        cfg.event(State::class, Event::class).trigger { visit("A") }.filter { false }
        cfg.event(StateA::class, EventA::class).goto { StateB() }
        cfg.event(StateB::class, EventB::class).goto { StateA() }
        val exe = cfg.createExecutor(Context(), StateA())
        assertVisited("")
        exe.fire(EventA())
        assertVisited("")
    }

    @Test fun `Multiple triggers can be triggered`() {
        cfg.event(State::class, Event::class).trigger { visit("0") }
        cfg.event(StateA::class, EventA::class).trigger { visit("1") }
        cfg.event(StateA::class, EventA::class).trigger { visit("2") }.goto { visit("3"); StateB() }
        cfg.event(StateB::class, EventB::class).goto { StateA() }
        val exe = cfg.createExecutor(Context(), StateA())
        exe.fire(EventA())
        assertVisited("0123")
    }

    @Test fun `Triggers are triggered bottom-up and in order of definition`() {
        cfg.event(StateA::class, EventA::class).trigger { visit("0") }
        cfg.event(StateA::class, EventA::class).trigger { visit("1") }.goto { visit("2"); StateB() }
        cfg.event(State::class, Event::class).trigger { visit("3") }
        cfg.event(StateA::class, EventA::class).trigger { visit("4") }
        cfg.event(StateB::class, EventB::class).trigger { visit("5") }.goto { StateA() }
        val exe = cfg.createExecutor(Context(), StateA())
        exe.fire(EventA())
        assertVisited("30142")
    }

    @Test fun `Only one transition from state is allowed`() {
        cfg.event(StateA::class, EventA::class).trigger { visit("Ta1") }.goto { visit("Ga1"); StateB() }
        cfg.event(StateA::class, EventA::class).trigger { visit("Ta2") }.goto { visit("Ga2"); StateB() }
        val exe = cfg.createExecutor(Context(), StateA())
        assertFailsWith(UnsupportedOperationException::class.java) {
            exe.fire(EventA())
        }
        assertVisited("") // nothing gets not executed
    }

    @Test fun `Fallback is triggered in declaration order, but used last for transition`() {
        cfg.event(StateA::class, Event::class).filter { true }.trigger { visit("t1") }
        cfg.event(StateA::class, Event::class).trigger { visit("fb") }.goto { visit("Gb"); StateB() }
        cfg.event(StateA::class, Event::class).filter { true }.trigger { visit("t2") }.goto { visit("Ga"); StateA() }
        val exe = cfg.createExecutor(Context(), StateA())
        exe.fire(Event())
        assertVisited("t1fbt2Ga")
    }

    @Test fun `Triggers are executed in order of hierarchy, closest one is used for transition`() {
        cfg.event(StateA::class, Event::class).trigger { visit("2") }
        cfg.event(State::class, Event::class).trigger { visit("0") }
        cfg.event(StateA::class, EventA::class).trigger { visit("3") }.goto { visit("Gx"); StateB() }
        cfg.event(State::class, EventA::class).trigger { visit("1") }.goto { visit("Gy"); StateB() }
        cfg.event(State::class, Event::class)
        val exe = cfg.createExecutor(Context(), StateA())
        exe.fire(EventA())
        assertVisited("0123Gx")
    }

    @Test fun `Multiple transition clash witch each-other`() {
        cfg.event(StateA::class, EventA::class)
                .trigger { visit("T1") }
                .filter { true }
                .goto { visit("G1"); StateB() }
        cfg.event(StateA::class, EventA::class)
                .trigger { visit("T2") }
                .filter { true }
                .goto { visit("G2"); StateB() }
        val exe = cfg.createExecutor(Context(), StateA())
        assertFailsWith(UnsupportedOperationException::class) {
            exe.fire(EventA())
        }
        assertVisited("")
    }

    @Test fun `Multiple transition can be defined for the same state as long as they are exclusive`() {
        cfg.event(StateA::class, EventA::class)
                .trigger { visit("T1") }
                .filter { false }
                .goto { visit("G1"); StateB() }
        cfg.event(StateA::class, EventA::class)
                .trigger { visit("T2") }
                .filter { true }
                .goto { visit("G2"); StateB() }
        val exe = cfg.createExecutor(Context(), StateA())
        exe.fire(EventA())
        assertVisited("T2G2")
    }

    @Test fun `Multiple transition can be defined for the same state as long as they are of different rank`() {
        cfg.event(StateA::class, Event::class)
                .trigger { visit("T1") }
                .filter { true }
                .goto { visit("G1"); StateB() }
        cfg.event(StateA::class, EventA::class)
                .trigger { visit("T2") }
                .filter { true }
                .goto { visit("G2"); StateB() }
        val exe = cfg.createExecutor(Context(), StateA())
        exe.fire(EventA())
        assertVisited("T1T2G2")
    }
}

private class Context {}
private open class State {}
private class StateA : State() {}
private class StateB : State() {}
private open class Event {}
private class EventA() : Event() {}
private class EventB() : Event() {}
