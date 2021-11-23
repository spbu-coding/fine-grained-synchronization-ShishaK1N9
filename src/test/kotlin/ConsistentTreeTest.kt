import consistent_tree.ConsistentTree

import org.junit.jupiter.api.BeforeEach

class ConsistentTreeTest : TreeTest() {

    @BeforeEach
    fun setup() {
        actualResult = ConsistentTree()
        expectingResult = ConsistentTree()
    }
}