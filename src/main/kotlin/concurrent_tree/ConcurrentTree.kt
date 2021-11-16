package concurrent_tree

import utils.ITree
import java.util.concurrent.locks.ReentrantLock

open class ConcurrentTree<KeyT : Comparable<KeyT>, ValueT> : ITree<KeyT, ValueT> {

    private var root: ConcurrentNode<KeyT, ValueT>? = null
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
        if (removingNode == root) {
            when (root!!.countOfChildren()) {
                0 -> {
                    root = null
                }
                1 -> {
                    root = root!!.leftChild ?: root!!.rightChild
                    root!!.parent = null
                    root!!.unlock()
                }
                2 -> {
                    val child = root!!.rightChild!!
                    child.lockFamily()
                    child.parent = null

                    root!!.leftChild!!.parent = null
                    root = root!!.leftChild

                    root!!.lockFamily()
                    val rightmostChild = root!!.moveToRightmost()

                    rightmostChild.insertNode(child)
                    rightmostChild.unlockFamily()
                    child.rightChild?.unlock()
                    child.leftChild?.unlock()
                }
            }
            lck.unlock()
            return true
        }
        lck.unlock()

        when (removingNode.countOfChildren()) {
            0 -> {
                val parent = removingNode.parent
                parent!!.whichChild(removingNode).set(null)
                parent.unlock()
            }
            1 -> {
                val removingNodeChild = removingNode.leftChild ?: removingNode.rightChild
                removingNodeChild!!.parent = removingNode.parent
                val parent = removingNode.parent
                parent!!.whichChild(removingNode).set(removingNodeChild)
                parent.unlock()
                removingNodeChild.unlock()
            }
            2 -> {
                val child = removingNode.rightChild!!
                val parent = removingNode.parent!!

                child.lockFamily()
                child.parent = null

                removingNode.leftChild!!.parent = parent
                parent.whichChild(removingNode).set(removingNode.leftChild)

                val newNode = parent.whichChild(removingNode.leftChild!!).get()!!
                newNode.rightChild?.lock()
                newNode.leftChild?.lock()

                val temp = newNode.moveToRightmost()

                temp.insertNode(child)
                child.unlock()
                child.rightChild?.unlock()
                child.leftChild?.unlock()
            }
        }

        return true
    }

    private fun ConcurrentNode<KeyT, ValueT>.insertNode(node: ConcurrentNode<KeyT, ValueT>) {
        node.parent = this
        this.rightChild = node
        this.unlockFamily()
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
            val child = ConcurrentNode(key, value)
            child.parent = temp

            if (temp.key < key) temp.rightChild = child
            else temp.leftChild = child

            temp.unlockFamily()
        }

        return true
    }

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
