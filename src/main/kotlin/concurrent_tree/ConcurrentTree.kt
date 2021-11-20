package concurrent_tree

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

    fun checkLocks() {
        var i = 0
        iterator().forEach { _ ->
            i++
        }
    }

    override fun search(key: KeyT): ValueT? {
        val temp = findNodeOrPotentialParent(key) ?: return null
        val value = if (temp.key == key) temp.value else null
        temp.unlockFamily()
        return value
    }

    override fun remove(key: KeyT): Boolean {
        val removingNode = findNodeOrPotentialParent(key) ?: return false

        if (removingNode.key != key) {
            removingNode.unlockFamily()
            return false
        }

        lck.lock()
        root?.let {
            if (removingNode != it) return@let

            it.lock()
            when (it.countOfChildren()) {
                0 -> root = null
                1 -> {
                    it.leftChild?.lock()
                    it.rightChild?.lock()

                    root = it.leftChild ?: it.rightChild!!
                    it.parent = null

                    it.unlock()
                }
                2 -> {
                    val rightChild = it.rightChild!!
                    val leftChild = it.leftChild!!

                    rightChild.lockFamily()
                    rightChild.parent = null

                    leftChild.lock()
                    leftChild.parent = null
                    root = leftChild

                    val rightmostChild = root!!.moveToRightmost()

                    rightmostChild.insertNode(rightChild)
                    rightChild.unlock()
                }
            }
            lck.unlock()
            return true
        }

        lck.unlock()

        when (removingNode.countOfChildren()) {
            0 -> {
                val parent = removingNode.parent!!
                parent.whichChild(removingNode).set(null)
                parent.unlock()
            }
            1 -> {
                val child = removingNode.leftChild ?: removingNode.rightChild!!
                child.lock()

                val parent = removingNode.parent!!
                child.parent = parent
                parent.whichChild(removingNode).set(child)

                child.unlockFamily()
            }
            2 -> {
                val rightChild = removingNode.rightChild!!
                val leftChild = removingNode.leftChild!!
                val parent = removingNode.parent!!

                rightChild.lockFamily()
                rightChild.parent = null

                leftChild.lock()
                leftChild.parent = parent
                parent.whichChild(removingNode).set(leftChild)

                val rightmostChild = parent.whichChild(leftChild).get()!!.moveToRightmost()

                rightmostChild.insertNode(rightChild)
                rightChild.unlock()
            }
        }

        return true
    }

    override fun insert(key: KeyT, value: ValueT): Boolean {
        lck.lock()
        val temp = findNodeOrPotentialParent(key) ?: run {
            root = ConcurrentNode(key, value)
            lck.unlock()
            return true
        }
        lck.unlock()

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
     * @param key key of searching node.
     * @return node, which can be a parent of node - if it doesn't exist, node with [key] - if it exists, *null* - if tree is empty.
     */
    private fun findNodeOrPotentialParent(key: KeyT): ConcurrentNode<KeyT, ValueT>? {
        var temp = root ?: return null
        temp.lockFamily()

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
            temp = child

            isNeededNode = temp.key.compareTo(key)
        }
        return temp
    }

    fun iterator(): Iterator<Pair<KeyT, ValueT>> {
        return object : Iterator<Pair<KeyT, ValueT>> {
            private var list = ArrayList<Pair<KeyT, ValueT>>()
            private var currentIndex = 0

            init {
                treeTraversal(root, list)
            }

            private fun treeTraversal(node: ConcurrentNode<KeyT, ValueT>?, list: ArrayList<Pair<KeyT, ValueT>>) {
                //println(node?.lck?.isLocked)
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
        if (this === other) return true
        if (this.javaClass != other?.javaClass) return false
        val otherIterator = (other as ConcurrentTree<*, *>).iterator()
        val iterator = this.iterator()

        while (otherIterator.hasNext() && iterator.hasNext()) {
            val node = iterator.next()
            val otherNode = otherIterator.next()
            if (node.first != otherNode.first || node.second != otherNode.second)
                return false
        }

        return !(otherIterator.hasNext() || iterator.hasNext())
    }

    override fun hashCode(): Int {
        fun calculateArrayStringRepresentation(node: ConcurrentNode<KeyT, ValueT>?, list: ArrayList<String>) {
            if (node != null) {
                list.add(node.key.toString() + node.value.toString())
                calculateArrayStringRepresentation(node.leftChild, list)
                calculateArrayStringRepresentation(node.rightChild, list)
            }
        }

        fun calculateHash(list: ArrayList<String>): Int {
            var result = ""
            for (element in list)
                result += element
            return result.hashCode()

        }

        val list = ArrayList<String>()
        calculateArrayStringRepresentation(root, list)

        return calculateHash(list)
    }
}
