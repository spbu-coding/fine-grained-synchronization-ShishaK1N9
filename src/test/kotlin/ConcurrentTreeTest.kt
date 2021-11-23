import concurrent_tree.ConcurrentTree

import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.junit.jupiter.api.Test

@Param.Params(
    Param(name = "key", gen = IntGen::class, conf = "-10:10"),
    Param(name = "value", gen = IntGen::class, conf = "-10:10")
)
class ConcurrentTreeTest : VerifierState() {

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
        .threads(5).actorsPerThread(3)
        .minimizeFailedScenario(false)
        .check(this::class.java)

    /**
     * Stress test, which runs on 500 iterations.
     */
    @Test
    fun stressTestWith500Iterations() = StressOptions()
        .iterations(500)
        .check(this::class.java)

    override fun extractState(): Any {
        val elements = mutableListOf<Pair<Int, Int>>()
        tree.iterator().forEach {
            elements.add(it)
        }
        return tree
    }
}