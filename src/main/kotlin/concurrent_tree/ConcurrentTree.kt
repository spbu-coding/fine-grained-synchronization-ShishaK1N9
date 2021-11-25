package concurrent_tree

import consistent_tree.ConsistentTree
import utils.ITree

import java.util.concurrent.locks.ReentrantLock

/**
 * Concurrent tree implementation.
 */
open class ConcurrentTree<KeyT : Comparable<KeyT>, ValueT> : ITree<KeyT, ValueT> {

    private var root: ConcurrentNode<KeyT, ValueT>? = null

    /**
     * Global tree lock. Uses if program works with root.
     */
    private var lck = ReentrantLock()

    /**
     * Locks global tree [lck].
     */
    private fun lock() = lck.lock()

    /**
     * Unlocks global tree [lck].
     */
    private fun unlock() = lck.unlock()

    override fun search(key: KeyT): ValueT? {
        val temp = findNodeOrPotentialParent(key) ?: run {
            unlock()
            return null
        }

        val value = if (temp.key == key) temp.value else null

        temp.unlockTreeIfRoot()
        temp.unlockFamily()

        return value
    }

    override fun remove(key: KeyT): Boolean {
        val removingNode = findNodeOrPotentialParent(key) ?: run {
            unlock()
            return false
        }

        if (removingNode.key != key) {
            removingNode.unlockTreeIfRoot()
            removingNode.unlockFamily()
            return false
        }

        removeNode(removingNode)
        return true
    }

    /**
     * Removes node which is not [root].
     * @param [removingNode] removing node which is not root.
     * @param [parent] removing node parent.
     * @param [leftChild] removing node left child, *null* - if it doesn't exist.
     * @param [rightChild] removing node right child, *null* - if it doesn't exist.
     */
    private fun removeNotRoot(
        removingNode: ConcurrentNode<KeyT, ValueT>,
        parent: ConcurrentNode<KeyT, ValueT>,
        leftChild: ConcurrentNode<KeyT, ValueT>?,
        rightChild: ConcurrentNode<KeyT, ValueT>?
    ) {
        when (removingNode.countOfChildren()) {
            0 -> {
                parent.whichChild(removingNode).set(null)
                parent.unlock()
            }
            1 -> {
                val child = leftChild ?: rightChild!!

                child.parent = parent
                parent.whichChild(removingNode).set(child)

                child.unlockFamily()
            }
            2 -> {
                rightChild!!.parent = null
                leftChild!!.parent = parent

                parent.whichChild(removingNode).set(leftChild)

                val rightmostChild = parent.whichChild(leftChild).get()!!.rightmostNode()

                rightmostChild.insertNode(rightChild)
                rightChild.unlock()
            }
        }
    }

    /**
     * Removes tree [root].
     */
    private fun removeRoot(leftChild: ConcurrentNode<KeyT, ValueT>?, rightChild: ConcurrentNode<KeyT, ValueT>?) {
        root?.let {
            when (it.countOfChildren()) {
                0 -> root = null
                1 -> {
                    root = it.leftChild ?: it.rightChild!!
                    root!!.parent = null

                    root!!.unlock()
                }
                2 -> {
                    rightChild!!.parent = null
                    leftChild!!.parent = null

                    root = leftChild

                    root!!.rightmostNode().insertNode(rightChild)
                    rightChild.unlock()
                }
            }
            unlock()
        }
    }

    private fun removeNode(removingNode: ConcurrentNode<KeyT, ValueT>) {
        removingNode.lockChildren()

        val leftChild = removingNode.leftChild
        val rightChild = removingNode.rightChild

        if (removingNode == root) {
            removeRoot(leftChild, rightChild)
        } else {
            val parent = removingNode.parent!!
            removeNotRoot(removingNode, parent, leftChild, rightChild)
        }
    }

    override fun insert(key: KeyT, value: ValueT): Boolean {
        val temp = findNodeOrPotentialParent(key) ?: run {
            root = ConcurrentNode(key, value)
            unlock()
            return true
        }

        temp.unlockTreeIfRoot()
        if (temp.key == key) {
            temp.unlockFamily()
            return false
        } else {
            val child = ConcurrentNode(key, value, temp)

            if (temp.key < key) temp.rightChild = child
            else temp.leftChild = child

            temp.unlockFamily()
        }

        return true
    }

    /**
     * Finds potential parent of node key.
     * @param [key] key of searching node.
     * @return node, which can be a parent of node - if it doesn't exist, node with [key] - if it exists, *null* - if tree is empty.
     */
    private fun findNodeOrPotentialParent(key: KeyT): ConcurrentNode<KeyT, ValueT>? {
        lock()
        root?.lock()
        var temp = root ?: return null
        var isNeededNode = temp.key.compareTo(key)

        while (isNeededNode != 0) {
            val leftChild = temp.leftChild
            val rightChild = temp.rightChild

            val child: ConcurrentNode<KeyT, ValueT>
            if (isNeededNode > 0) {
                child = leftChild ?: return temp
                temp.moveToChild(child)
            } else {
                child = rightChild ?: return temp
                temp.moveToChild(child)
            }
            temp.unlockTreeIfRoot()
            temp = child

            isNeededNode = temp.key.compareTo(key)
        }
        return temp
    }

    /**
     * Unlocks global tree lock, if node is [root].
     */
    private fun ConcurrentNode<KeyT, ValueT>.unlockTreeIfRoot() {
        if (this == root) this@ConcurrentTree.unlock()
    }

    /**
     * Locks the whole tree.
     */
    private fun lockTree(lockingNode: ConcurrentNode<KeyT, ValueT>?) {
        lockingNode?: return
        lockingNode.lock()
        lockTree(lockingNode.leftChild)
        lockTree(lockingNode.rightChild)
    }

    /**
     * Unlocks the whole tree.
     */
    private fun unlockTree(unlockingNode: ConcurrentNode<KeyT, ValueT>?) {
        unlockingNode?: return
        unlockTree(unlockingNode.leftChild)
        unlockTree(unlockingNode.rightChild)
        unlockingNode.unlock()
    }

    fun iterator(): Iterator<Pair<KeyT, ValueT>> {
        return object : Iterator<Pair<KeyT, ValueT>> {
            private var list = ArrayList<Pair<KeyT, ValueT>>()
            private var currentIndex = 0

            init {
                treeTraversal(root, list)
            }

            private fun treeTraversal(node: ConcurrentNode<KeyT, ValueT>?, list: ArrayList<Pair<KeyT, ValueT>>) {
                if (node != null) {
                    treeTraversal(node.leftChild, list)
                    list.add(Pair(node.key, node.value))
                    treeTraversal(node.rightChild, list)
                }
            }

            override fun hasNext(): Boolean {
                return currentIndex < list.size
            }

            override fun next(): Pair<KeyT, ValueT> {
                return list[currentIndex++]
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ConcurrentTree<*, *>) return false
        if (this === other) return true
        lockTree(root)

        val (thisElements, otherElements) =
            arrayOf(mutableListOf<Pair<*, *>>(), mutableListOf())
        this.iterator().forEach {
            thisElements.add(it)
        }
        (other).iterator().forEach {
            otherElements.add(it)
        }

        unlockTree(root)
        return thisElements == otherElements
    }

    override fun hashCode(): Int {
        lockTree(root)

        val elements = mutableListOf<Pair<KeyT, ValueT>>()
        this.iterator().forEach {
            elements.add(it)
        }

        unlockTree(root)
        return elements.hashCode()
    }
}
