import binary_tree.Tree
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration

abstract class TreeTest {
    lateinit var actualResult: Tree<Int, String>
    lateinit var expectingResult: Tree<Int, String>

/*-------------------------------------------------Positive scenarios-------------------------------------------------*/

    /*Edge cases*/

    @Test
    fun `insert should insert element to an empty tree`() {
        val key = 0
        val value = "I am root!"

        assertTrue(actualResult.insert(key, value))
        actualResult.iterator().forEach {
            assertTrue(it.first == key)
            assertTrue(it.second == value)
        }
    }

    @Test
    fun `insert shouldn't insert elements with the same keys`() {
        val (key1, key2) = arrayOf(0, 0)
        val (value1, value2) = arrayOf("I am root!", "I am not root(")

        assertTrue(actualResult.insert(key1, value1))
        assertTrue(!actualResult.insert(key2, value2))
    }
}
