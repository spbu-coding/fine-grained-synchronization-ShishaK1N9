package concurrent_tree

import java.util.concurrent.locks.ReentrantLock

internal class ConcurrentNode<KeyT : Comparable<KeyT>, ValueT>(var key: KeyT, var value: ValueT) {

    var parent: ConcurrentNode<KeyT, ValueT>? = null
    var leftChild: ConcurrentNode<KeyT, ValueT>? = null
    var rightChild: ConcurrentNode<KeyT, ValueT>? = null

    var lck = ReentrantLock()

    private fun ReentrantLock.fullUnlock() {
        repeat(holdCount) { unlock() }
    }

    internal fun lock() {
        lck.lock()
    }

    internal fun unlock() {
        lck.unlock()
    }

    internal fun whichChild(node: ConcurrentNode<KeyT, ValueT>) =
        if (node.key == leftChild?.key) ::leftChild
        else ::rightChild

    internal fun countOfChildren() =
        (leftChild?.let { 1 } ?: 0) + (rightChild?.let { 1 } ?: 0)

    internal fun lockFamily() {
        parent?.lock()
        lock()
        leftChild?.lock()
        rightChild?.lock()
    }

    internal fun unlockFamily() {
        unlock()
        parent?.unlock()
        leftChild?.unlock()
        rightChild?.unlock()
    }
}