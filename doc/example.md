Example
===
The goal is not to construct best state machine for the Door Problem, it's just about presenting some available mechanisms.

# Simple case
![Simple door machine](simple-door-machine.png)

# States
Let's start with possible states. The door can be in two states: the door can be open or closed.

```kotlin
interface DoorState
class OpenState: DoorState
class ClosedState: DoorState
```

# Events
The next thing is define possible events. There are two possible events: door can be opened of closed.

```kotlin
interface DoorEvent
class OpenEvent: DoorEvent
class CloseEvent: DoorEvent
```

# Context
Context is not really needed for this task, but let's assume context in this example will be the object allowing the door to make sounds. Let's call it `Emitter`:

```kotlin
class Emitter {
    fun sound(text: String) = println(text)
}
```

# Configuration
So, let's start configuring our state machine. We need a configurator, and we will use `Emitter` as context, `DoorState` as base class for states and `DoorEvent` as base class for events:

```kotlin
var configurator =
    StateMachine.createConfigurator<Emitter, DoorState, DoorEvent>()
```

# Rules
Note: as code below uses extension functions you need to add specific import statement (and IDE may not help you with that):

```kotlin
import org.softpark.stateful4k.extensions.*
```

Rules are quite simple:

* When doors are closed handle open event

```kotlin
configurator
    .event(ClosedState::class, OpenEvent::class)
    .goto { OpenState() }
```

* When doors are open handle close event

```kotlin
configurator
    .event(OpenState::class, CloseEvent::class)
    .goto { ClosedState() }
```

* Stay in the same state in all other cases

```kotlin
configurator
    .event(DoorState::class, DoorEvent::class)
    .loop()
```

* When doors are opened make the 'Squeak' sound

```kotlin
configurator
    .state(OpenState::class)
    .enter { context.sound("Squeak!") }
```

* When doors are closed make the 'Bang!' sound

```kotlin
configurator
    .state(ClosedState::class)
    .enter { context.sound("Bang!") }
```

```kotlin
configurator.

Definition
---
There are doors and they can be open or closed, but also locked and unlocked. Please note that there are 4 distinctive states: open/unlocked, closed/unlocked, closed/locked and open/locked (ther last one is about door being wide-open but with lock in locked position).

![DoorMachine](door-state-machine.png)

States
---
There is a base abstract `DoorState` allowing locking and unlocking:

```kotlin
abstract class DoorState(locked: Boolean) {
    var locked: Boolean = locked; get private set
    fun lock() { locked = true }
    fun unlock() { locked = false }
}
```

and two concrete states, `DoorOpened` and `DoorClosed`:

```kotlin
class DoorClosed(locked: Boolean): DoorState(locked) {}
class DoorOpened(locked: Boolean): DoorState(locked) {}
```

Events
---
There is a base (empty) interface for all door events. It could actually be modelled as `abstract class` for a price of some extra typing:

```kotlin
interface DoorEvent
```

There are four events: `OpenEvent`, `CloseEvent`, `LockEvent` and `UnlockEvent`. They all implement `DoorEvent` and carry no data:

```kotlin
class LockEvent : DoorEvent
class UnlockEvent : DoorEvent
class CloseEvent : DoorEvent
class OpenEvent : DoorEvent
```

Context
---
In this case there is no specific context, but for sake of this exercise let's make one up: hypothetical `SoundEmitter` or just `Emitter`.

```kotlin
interface Emitter {
    fun sound(sound: String)
}
```

In this exercise we can implement `ConsoleEmitter` which will just print message to the console:

```kotlin
class ConsoleEmitter : Emitter {
    override fun sound(sound: String) = println(sound)
}
```

Configuration
---
Let's create a configurator. We will just say that we are going to configure state machine with `Emitter` as `Context`, `DoorState` as `State` and `DoorEvent` as `Event`.

```kotlin
val configurator = StateMachine.createConfigurator<Emitter, DoorState, DoorEvent>()
```

Let's define the rules.

* in any state on unlock event (`event`), if door is locked (`filter`) - make the 'Click!' sound, unlock the door but stay in the same state (`loop`)
* in any state on lock event, if door is unlocked - make the 'Clack!' sound, lock the door and stay in the same state

```kotlin
configurator
    .event(DoorState::class, UnlockEvent::class)
    .filter { state.locked }
    .loop { context.sound("Click!"); state.unlock() }
configurator
    .event(DoorState::class, LockEvent::class)
    .filter { !state.locked }
    .loop { context.sound("Clack!"); state.lock() }
```

* in closed state on open event if the door is locked - make the 'Click! Click!' but don't change the state (as doors are locked)
* in closed state on open event if all the other rules are not matched (note lack of `filter`) - make the 'Click!' and 'Squeak!' sound to transition (`goto`) to opened state (or in plain English - open the door)

```kotlin
configurator
    .event(DoorClosed::class, OpenEvent::class)
    .filter { state.locked }
    .loop { context.sound("Click! Click!") }
configurator
    .event(DoorClosed::class, OpenEvent::class)
    .goto { context.sound("Click! Squeak!"); DoorOpened(false) }
```

Stop here for the while. There are two kinds of rules in `Stateful`.
