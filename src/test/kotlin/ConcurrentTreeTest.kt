import concurrent_tree.ConcurrentTree

import org.jetbrains.kotlinx.lincheck.LinChecker
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTest
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.junit.jupiter.api.Test

@Param.Params(
    Param(name = "key", gen = IntGen::class, conf = "-10:10"),
    Param(name = "value", gen = IntGen::class, conf = "-10:10")
)
@StressCTest(
    actorsBefore = 2,
    threads = 3, actorsPerThread = 3,
    actorsAfter = 0,
    minimizeFailedScenario = false
)
class ConcurrentTreeTest : VerifierState() {

    private val tree = ConcurrentTree<Int, Int>()

    @Operation
    fun search(@Param(name = "key") key: Int) = tree.search(key)

    @Operation
    fun insert(@Param(name = "key") key: Int, @Param(name = "value") value: Int) = tree.insert(key, value)

    @Operation
    fun remove(@Param(name = "key") key: Int) = tree.remove(key)

    @Test
    fun stressTest() = LinChecker.check(this::class.java)

    override fun extractState(): Any {
        val elements = mutableListOf<Pair<Int, Int>>()
        tree.iterator().forEach {
            elements.add(it)
        }
        return tree
    }
}