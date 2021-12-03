package concurrent_tree

import java.util.concurrent.locks.ReentrantLock

/**
 * Concurrent node implementation.
 * @property [key] node key
 * @property [value] node value
 * @property [parent] node parent (default value is *null*)
 */
open class ConcurrentNode<KeyT : Comparable<KeyT>, ValueT>(
    initialKey: KeyT,
    initialValue: ValueT,
    var parent: ConcurrentNode<KeyT, ValueT>? = null
) {

    var key = initialKey
        private set
    var value = initialValue
        private set

    var leftChild: ConcurrentNode<KeyT, ValueT>? = null
    var rightChild: ConcurrentNode<KeyT, ValueT>? = null

    var lck = ReentrantLock()

    /**
     * Locks node [lck]
     */
    fun lock() {
        lck.lock()
    }

    /**
     * Unlocks node [lck]
     */
    fun unlock() {
        lck.unlock()
    }

    /**
     * Locks node and its parent.
     */
    fun lockFamily() {
        lock()
        parent?.lock()
    }

    /**
     * Unlocks node and its parent.
     */
    fun unlockFamily() {
        unlock()
        parent?.unlock()
    }

    /**
     * Locks node children.
     */
    fun lockChildren() {
        leftChild?.lock()
        rightChild?.lock()
    }

    /**
     * Carefully unlocks node parent and locks node needed child.
     * @param [child] needed child
     */
    fun moveToChild(child: ConcurrentNode<KeyT, ValueT>) {
        child.lockFamily()
        parent?.unlock()
        unlock()
    }

    /**
     * Carefully moves to rightmost child.
     * @return rightmost node in the subtree with current node as a root.
     */
    fun rightmostNode(): ConcurrentNode<KeyT, ValueT> {
        var temp: ConcurrentNode<KeyT, ValueT> = this
        while (temp.rightChild != null) {
            temp.rightChild?.let {
                it.lockFamily()
                temp.unlockFamily()
                temp = it
            }
        }

        return temp
    }

    /**
     * Inserts node to a subtree with current node as a root.
     * @param [node] subtree root.
     */
    fun insertNode(node: ConcurrentNode<KeyT, ValueT>) {
        node.parent = this
        this.rightChild = node
        this.unlockFamily()
    }

    /**
     * Detects which child the node is.
     * @param [node] child which needed to be identified.
     * @return [leftChild] link - if [node] is [leftChild], [rightChild] link - if node is [rightChild].
     */
    fun whichChild(node: ConcurrentNode<KeyT, ValueT>) =
        if (node.key == leftChild?.key) ::leftChild
        else ::rightChild

    /**
     * Count children of current node.
     * @return 0 - if node is a leave, 1 - if node has only one child and 2 - if node has two children.
     */
    fun countOfChildren() =
        (leftChild?.let { 1 } ?: 0) + (rightChild?.let { 1 } ?: 0)
}