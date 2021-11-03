import binary_tree.Tree
import org.junit.jupiter.api.BeforeEach

class ConsistentTreeTest : TreeTest() {

    @BeforeEach
    fun setup() {
        actualResult = Tree()
        expectingResult = Tree()
    }
}