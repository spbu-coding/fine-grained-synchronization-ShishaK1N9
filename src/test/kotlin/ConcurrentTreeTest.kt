import concurrent_tree.ConcurrentTree
import utils.ITree

import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap

@Param.Params(
    Param(name = "key", gen = IntGen::class, conf = "-10:10"),
    Param(name = "value", gen = IntGen::class, conf = "-10:10")
)
class ConcurrentTreeTest : VerifierState() {

    class SimpleTree : ITree<Int, Int>, VerifierState() {

        private val map = ConcurrentHashMap<Int, Int>()

        @Operation
        override fun search(key: Int) =
            if (map.containsKey(key)) map[key]
            else null

        @Operation
        override fun remove(key: Int) = map.remove(key) != null

        @Operation
        override fun insert(key: Int, value: Int) =
            if (map.containsKey(key)) false
            else {
                map[key] = value
                true
            }

        override fun extractState() = map
    }

    private val tree = ConcurrentTree<Int, Int>()

    @Operation
    fun search(@Param(name = "key") key: Int) = tree.search(key)

    @Operation
    fun insert(@Param(name = "key") key: Int, @Param(name = "value") value: Int) = tree.insert(key, value)

    @Operation
    fun remove(@Param(name = "key") key: Int) = tree.remove(key)

    /**
     * Stress test, which runs on 3 threads.
     */
    @Test
    fun stressTestOn3Thread() = StressOptions()
        .actorsBefore(1)
        .threads(3).actorsPerThread(3)
        .minimizeFailedScenario(false)
        .check(this::class.java)

    /**
     * Stress test, which runs on 5 threads.
     */
    @Test
    fun stressTestOn5Threads() = StressOptions()
        .actorsBefore(1)
        .threads(5).actorsPerThread(5)
        .minimizeFailedScenario(false)
        .check(this::class.java)

    /**
     * Stress test, which runs 500 scenarios.
     */
    @Test
    fun stressTestWith500Scenarios() = StressOptions()
        .iterations(500)
        .check(this::class.java)

    /**
     * Compares concurrent tree implementation with concurrent hash map implementation.
     */
    @Test
    fun modelCheckingTest() = ModelCheckingOptions()
        .sequentialSpecification(SimpleTree::class.java)
        .check(this::class.java)

    override fun extractState(): Any {
        val elements = mutableListOf<Pair<Int, Int>>()
        tree.iterator().forEach {
            elements.add(it)
        }
        return tree
    }
}