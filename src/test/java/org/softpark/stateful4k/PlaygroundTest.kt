package org.softpark.stateful4k

import org.junit.Test
import org.softpark.stateful4k.extensions.distanceFrom
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlaygroundTest {
    interface SomeInterface {}
    interface SomeDerivedInterface: SomeInterface {}
    open class SomeClass : SomeInterface {}
    class SomeDerivedClass() : SomeClass() {}
    class SomeDerivedClassWithReimplementingInterface() : SomeClass(), SomeInterface {}
    class SomeClassImplementingDerivedInterface : SomeDerivedInterface {}

    @Test fun `Different objects of the same class return same class instance`() {
        val x = SomeClass()
        val y = SomeClass()
        assertFalse { x == y }
        assertTrue { x.javaClass == y.javaClass }
        assertTrue { x.javaClass == SomeClass::class.java }
    }

    @Test fun `Base class can be read with reflection`() {
        val x = SomeDerivedClass()
        assertTrue { x.javaClass.superclass == SomeClass::class.java }
    }

    @Test fun `Can get a list of implemented interfaces`() {
        val x = SomeClass()
        assertTrue { x.javaClass.interfaces.contains(SomeInterface::class.java) }
    }

    @Test fun `Derived class implements interface from base by inheritance`() {
        val x = SomeDerivedClass()
        assertTrue { x.javaClass.superclass.interfaces.contains(SomeInterface::class.java) }
    }

    @Test fun `Derived class reimplementing interface from base officially implements it`() {
        val x = SomeDerivedClassWithReimplementingInterface()
        assertTrue { x.javaClass.interfaces.contains(SomeInterface::class.java) }
        assertTrue { x.javaClass.superclass.interfaces.contains(SomeInterface::class.java) }
    }

    @Test fun `Testing which class inherits from which`() {
        assertTrue { SomeClass::class.java.isAssignableFrom(SomeDerivedClass::class.java) }
        assertFalse { SomeDerivedClass::class.java.isAssignableFrom(SomeClass::class.java) }
    }

    @Test fun `Derived class does not officially implement interface from base`() {
        val x = SomeDerivedClass()
        assertFalse { x.javaClass.interfaces.contains(SomeInterface::class.java) }
    }

    @Test fun `Class implementing derived interface does not officially implement base interface`() {
        assertFalse {
            SomeClassImplementingDerivedInterface::class.java.interfaces.contains(SomeInterface::class.java)
        }
    }

    @Test fun `Distance to itself is 0`() {
        val c = SomeClass::class.java
        val d = c.distanceFrom(c)
        assertTrue { d != null }
        assertTrue { d == 0 }
    }

    @Test fun `Distance to immediate parent is 1`() {
        val c = SomeClass::class.java
        val d = c.distanceFrom(Any::class.java)
        assertTrue { d != null }
        assertTrue { d == 1 }
    }

    @Test fun `Distance to distant parent is more than 1`() {
        val c = SomeDerivedClass::class.java
        val d = c.distanceFrom(Any::class.java)
        assertTrue { d != null }
        assertTrue { d == 2 }
    }

    @Test fun `Distance to interface is 1`() {
        val c = SomeClass::class.java
        val d = c.distanceFrom(SomeInterface::class.java)
        assertTrue { d != null }
        assertTrue { d == 1 }
    }

    @Test fun `Distance to inherited interface is more than 1`() {
        val c = SomeDerivedClass::class.java
        val d = c.distanceFrom(SomeInterface::class.java)
        assertTrue { d != null }
        assertTrue { d == 2 }
    }

    @Test fun `Distance to reimplemented interface is 1 again`() {
        val c = SomeDerivedClassWithReimplementingInterface::class.java
        val d = c.distanceFrom(SomeInterface::class.java)
        assertTrue { d != null }
        assertTrue { d == 1 }
    }

    @Test fun `Distance to base interface is more than 1`() {
        val c = SomeClassImplementingDerivedInterface::class.java
        val d = c.distanceFrom(SomeInterface::class.java)
        assertNotNull(d)
        assertTrue { d == 2 }
    }
}
