import concurrent_tree.ConcurrentTree
import consistent_tree.ConsistentTree
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

class ConcurrentTreeTest : TreeTest() {

    lateinit var threads: MutableList<Thread>
    lateinit var threads2: MutableList<Thread>

    @BeforeEach
    fun setup() {
        actualResult = ConcurrentTree()
        expectingResult = ConcurrentTree()
        threads = mutableListOf()
        threads2 = mutableListOf()
    }

    @RepeatedTest(1000)
    fun test() {
        for(i in 1..4) {
            val task = thread {
                for(j in 1..10) {
                    actualResult.insert(i * 1000 + j, "${i * 1000 + j}")
                }
            }
            threads.add(task)
        }

        threads.forEach {
            it.join()
        }

        for(i in 1..4) {
            for(j in 1..10) {
                assertEquals("${i * 1000 + j}", actualResult.search(i * 1000 + j))
            }
        }
    }

    @RepeatedTest(1000)
    fun test2() {
        for(i in 1..4) {
            val task = thread {
                for(j in 1..10) {
                    actualResult.insert(i * 1000 + j, "${i * 1000 + j}")
                }
            }
            threads.add(task)
        }

        threads.forEach {
            it.join()
        }

        for(i in 1..4) {
            val task = thread {
                for(j in 1..10) {
                    assertTrue(actualResult.remove(i * 1000 + j))
                }
            }
            threads2.add(task)
        }

        threads2.forEach {
            it.join()
        }

        for(i in 1..4) {
            for(j in 1..10) {
                assertNull(actualResult.search(i * 1000 + j))
            }
        }
    }
}