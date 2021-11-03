import concurrent_tree.ConcurrentTree
import consistent_tree.ConsistentTree
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

class ConcurrentTreeTest : TreeTest() {

    lateinit var threads: MutableList<Thread>

    @BeforeEach
    fun setup() {
        actualResult = ConcurrentTree()
        expectingResult = ConcurrentTree()
        threads = mutableListOf()
    }

    @RepeatedTest(10)
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

        for(i in 1..10) {
            for(j in 1..10) {
                assertEquals("${i * 1000 + j}", actualResult.search(i * 1000 + j))
            }
        }
    }
}