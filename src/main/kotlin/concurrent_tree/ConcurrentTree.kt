package concurrent_tree

import java.util.concurrent.locks.ReentrantLock

open class ConcurrentTree<KeyT : Comparable<KeyT>, ValueT> {

    private var root: ConcurrentNode<KeyT, ValueT>? = null

    private var lck = ReentrantLock()

    fun search(key: KeyT): ValueT? {
        val temp = findNodeOrPotentialParent(key) ?: return null
        val value = if (temp.key == key) temp.value else null
        temp.unlockFamily()
        return value
    }

    fun remove(key: KeyT): Boolean {
        val removingNode = findNodeOrPotentialParent(key) ?: return false

        if (removingNode.key != key) {
            removingNode.unlockFamily()
            return false
        }

        if (removingNode == root) {
            when (root!!.countOfChildren()) {
                0 -> root = null
                1 -> {
                    root = root!!.leftChild ?: root!!.rightChild
                    root!!.parent = null
                    root!!.unlock()
                }
                2 -> {
                    val child = root!!.rightChild!!
                    child.parent = null

                    root!!.leftChild!!.parent = null
                    root = root!!.leftChild
                    root!!.unlock()
                    insertNode(child)
                }
            }
            return true
        }

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
                child.parent = null

                removingNode.leftChild!!.parent = removingNode.parent
                removingNode.parent!!.whichChild(removingNode).set(removingNode.leftChild)
                removingNode.parent!!.whichChild(removingNode.leftChild!!).get()!!.unlock()
                removingNode.parent!!.unlock()

                insertNode(child)
            }
        }

        return true
    }

    fun insert(key: KeyT, value: ValueT): Boolean {
        val temp = findNodeOrPotentialParent(key) ?: run {
            lck.lock()
            root = ConcurrentNode(key, value)
            lck.unlock()
            return true
        }

        if (temp.key == key) {
            temp.unlockFamily()
            return false
        } else if (temp.key < key) {
            val child = ConcurrentNode(key, value)
            child.lock()
            temp.rightChild = child
            temp.rightChild!!.parent = temp
        } else {
            val child = ConcurrentNode(key, value)
            child.lock()
            temp.leftChild = child
            temp.leftChild!!.parent = temp
        }
        temp.unlockFamily()
        return true
    }

    private fun insertNode(node: ConcurrentNode<KeyT, ValueT>): Boolean {
        val temp = findNodeOrPotentialParent(node.key)!!
        node.parent = temp
        temp.rightChild = node
        temp.unlockFamily()
        return true
    }

    private fun findNodeOrPotentialParent(key: KeyT): ConcurrentNode<KeyT, ValueT>? {
        var temp = root ?: return null
        temp.lock()

        var isNeededNode = temp.key.compareTo(key)

        while (isNeededNode != 0) {
            if (isNeededNode > 0) {
                val child = temp.leftChild ?: return temp
                child.leftChild?.lock()
                child.rightChild?.lock()
                temp.parent?.unlock()
                temp.rightChild?.unlock()
                temp = child
            } else {
                val child = temp.rightChild ?: return temp
                child.leftChild?.lock()
                child.rightChild?.lock()
                temp.parent?.unlock()
                temp.leftChild?.unlock()
                temp = child
            }
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
