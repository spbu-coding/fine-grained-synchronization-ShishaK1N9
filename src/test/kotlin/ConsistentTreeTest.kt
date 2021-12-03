import consistent_tree.ConsistentTree

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ConsistentTreeTest {

    var actualResult = ConsistentTree<Int, String>()
    var expectingResult = ConsistentTree<Int, String>()


    @Nested
    inner class InsertTests {
        @Test
        fun `insert should insert element to an empty tree`() {
            val key = 0
            val value = "I am root!"

            assertTrue(actualResult.insert(key, value))
            assertEquals(actualResult.search(key), value)
        }

        @Test
        fun `insert shouldn't insert elements with the same keys`() {
            val (key1, key2) = arrayOf(0, 0)
            val (value1, value2) = arrayOf("I am root!", "I am not root(")

            assertTrue(actualResult.insert(key1, value1))
            assertFalse(actualResult.insert(key2, value2))
        }

        @Test
        fun `insert should insert right child`() {
            val (key1, key2) = arrayOf(0, 1)
            val (value1, value2) = arrayOf("I am root!", "I am not root(")

            assertTrue(actualResult.insert(key1, value1))
            assertTrue(actualResult.insert(key2, value2))

            assertEquals(actualResult.search(key2), value2)
        }

        @Test
        fun `insert should insert left child`() {
            val (key1, key2) = arrayOf(0, -1)
            val (value1, value2) = arrayOf("I am root!", "I am not root(")

            assertTrue(actualResult.insert(key1, value1))
            assertTrue(actualResult.insert(key2, value2))

            assertEquals(actualResult.search(key2), value2)
        }
    }


    @Nested
    inner class RemoveTests {
        @Nested
        inner class TestRoot {
            @Test
            fun `remove shouldn't remove from empty tree`() {
                val key = 0

                assertFalse(actualResult.remove(key))
            }

            @Test
            fun `remove shouldn't remove non-existent node`() {
                val (key1, key2, key3) = arrayOf(0, 1, 2)
                val (value1, value2) = arrayOf("I am root!", "I am not root(")

                actualResult.insert(key1, value1)
                actualResult.insert(key2, value2)
                assertFalse(actualResult.remove(key3))
            }

            @Test
            fun `remove should remove root from tree with only root`() {
                val key = 0
                val value = "I am root!"

                actualResult.insert(key, value)

                assertTrue(actualResult.remove(key))
                assertNull(actualResult.search(key))
            }

            @Test
            fun `remove should remove root with one left child `() {
                val (key1, key2) = arrayOf(0, -1)
                val (value1, value2) = arrayOf("I am root!", "I am not root(")

                actualResult.insert(key1, value1)
                actualResult.insert(key2, value2)

                assertTrue(actualResult.remove(key1))
                assertNull(actualResult.search(key1))
            }

            @Test
            fun `remove should remove root with one right child `() {
                val (key1, key2) = arrayOf(0, 1)
                val (value1, value2) = arrayOf("I am root!", "I am not root(")

                actualResult.insert(key1, value1)
                actualResult.insert(key2, value2)

                assertTrue(actualResult.remove(key1))
                assertNull(actualResult.search(key1))
            }

            @Test
            fun `remove should remove root with children`() {
                val (key1, key2, key3) = arrayOf(0, 1, -1)
                val (value1, value2, value3) = arrayOf("I am root!", "I am not root(", "I am not root too(")

                actualResult.insert(key1, value1)
                actualResult.insert(key2, value2)
                actualResult.insert(key3, value3)

                assertTrue(actualResult.remove(key1))
                assertNull(actualResult.search(key1))
            }
        }

        @Nested
        inner class TestOtherNodes {
            @Test
            fun `remove should remove right node without children`() {
                val (keyR, key) = arrayOf(0, 1)
                val (valueR, value) = arrayOf("I am root!", "I am not root(")

                actualResult.insert(keyR, valueR)
                actualResult.insert(key, value)

                assertTrue(actualResult.remove(key))
                assertNull(actualResult.search(key))
            }

            @Test
            fun `remove should remove left node without children`() {
                val (keyR, key) = arrayOf(0, -1)
                val (valueR, value) = arrayOf("I am root!", "I am not root(")

                actualResult.insert(keyR, valueR)
                actualResult.insert(key, value)

                assertTrue(actualResult.remove(key))
                assertNull(actualResult.search(key))
            }

            @Test
            fun `remove should remove node with one left child`() {
                val (keyR, key1, key2) = arrayOf(0, 2, 1)
                val (valueR, value1, value2) = arrayOf("I am root!", "I am not root(", "I am not root too(")

                actualResult.insert(keyR, valueR)
                actualResult.insert(key1, value1)
                actualResult.insert(key2, value2)

                assertTrue(actualResult.remove(key1))
                assertNull(actualResult.search(key1))
            }

            @Test
            fun `remove should remove node with one right child`() {
                val (keyR, key1, key2) = arrayOf(0, 1, 2)
                val (valueR, value1, value2) = arrayOf("I am root!", "I am not root(", "I am not root too(")

                actualResult.insert(keyR, valueR)
                actualResult.insert(key1, value1)
                actualResult.insert(key2, value2)

                assertTrue(actualResult.remove(key1))
                assertNull(actualResult.search(key1))
            }

            @Test
            fun `remove should remove node with children`() {
                val (keyR, key1, key2, key3) = arrayOf(0, 2, 1, 3)
                val (valueR, value1, value2, value3) =
                    arrayOf("I am root!", "I am not root(", "I am not root too(", "(")

                actualResult.insert(keyR, valueR)
                actualResult.insert(key1, value1)
                actualResult.insert(key2, value2)
                actualResult.insert(key3, value3)

                assertTrue(actualResult.remove(key1))
                assertNull(actualResult.search(key1))
            }
        }
    }


    @Nested
    inner class SearchTests {
        @Test
        fun `search shouldn't find node in an empty tree`() {
            val key = 0

            assertNull(actualResult.search(key))
        }

        @Test
        fun `search should find root`() {
            val key = 0
            val value = "I am root!"

            actualResult.insert(key, value)
            assertEquals(actualResult.search(key), value)
        }

        @Test
        fun `search shouldn't find non-existent node`() {
            val (key1, key2) = arrayOf(0, 1)
            val value = "I am root!"

            actualResult.insert(key1, value)
            assertNull(actualResult.search(key2))
        }
    }

    @Nested
    inner class HashcodeTests {
        @Test
        fun `same trees should have equals hashcode`() {
            actualResult.insert(1, "a")
            expectingResult.insert(1, "a")

            assertEquals(expectingResult.hashCode(), actualResult.hashCode())
        }

        @Test
        fun `different trees should have not equals hashcode`() {
            actualResult.insert(1, "a")
            expectingResult.insert(1, "b")

            assertNotEquals(expectingResult.hashCode(), actualResult.hashCode())
        }
    }

    @Nested
    inner class EqualsTests {
        @Test
        fun `same trees should be equals`() {
            actualResult.insert(1, "a")
            expectingResult.insert(1, "a")

            assertEquals(expectingResult, actualResult)
        }

        @Test
        fun `tree should not be equals object with other type`() {
            expectingResult.insert(1, "a")

            assertNotEquals(expectingResult, 1)
        }

        @Test
        fun `tree should not be equals null`() {
            expectingResult.insert(1, "a")

            assertNotEquals(expectingResult, null)
        }

        @Test
        fun `tree should be equals itself`() {
            actualResult.insert(1, "a")

            assertEquals(actualResult, actualResult)
        }

        @Test
        fun `different trees with equal keys should not be equals`() {
            actualResult.insert(1, "a")
            expectingResult.insert(1, "b")

            assertNotEquals(expectingResult, actualResult)
        }

        @Test
        fun `different trees should not be equals`() {
            actualResult.insert(1, "a")
            expectingResult.insert(2, "b")

            assertNotEquals(expectingResult, actualResult)
        }

        @Test
        fun `larger tree should not be equals to the smaller tree`() {
            expectingResult.insert(1, "a")
            actualResult.insert(1, "a")
            actualResult.insert(2, "b")

            assertNotEquals(expectingResult, actualResult)
        }

        @Test
        fun `smaller tree should not be equals to the larger tree`() {
            actualResult.insert(1, "a")
            expectingResult.insert(1, "a")
            expectingResult.insert(2, "b")

            assertNotEquals(expectingResult, actualResult)
        }
    }
}
