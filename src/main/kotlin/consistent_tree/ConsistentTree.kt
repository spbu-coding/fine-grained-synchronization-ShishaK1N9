package consistent_tree

import utils.ITree
import java.security.Key

open class ConsistentTree<KeyT : Comparable<KeyT>, ValueT> : ITree<KeyT, ValueT> {

    private var root: ConsistentNode<KeyT, ValueT>? = null

    override fun search(key: KeyT): ValueT? {
        val temp = findNodeOrPotentialParent(key) ?: return null
        return if (temp.key == key) temp.value else null
    }

    override fun remove(key: KeyT): Boolean {
        val removingNode = findNodeOrPotentialParent(key) ?: return false

        if (removingNode.key != key) return false

        if (removingNode == root) {
            when (root!!.countOfChildren()) {
                0 -> root = null
                1 -> {
                    root = root!!.leftChild ?: root!!.rightChild
                    root!!.parent = null
                }
                else -> {
                    val child = root!!.rightChild!!
                    child.parent = null

                    root!!.leftChild!!.parent = null
                    root = root!!.leftChild

                    insertNode(child)
                }
            }
            return true
        }

        when (removingNode.countOfChildren()) {
            0 -> removingNode.parent!!.whichChild(removingNode).set(null)
            1 -> {
                val removingNodeChild = removingNode.leftChild ?: removingNode.rightChild
                removingNodeChild!!.parent = removingNode.parent
                removingNode.parent!!.whichChild(removingNode).set(removingNodeChild)
            }
            else -> {
                val child = removingNode.rightChild!!
                child.parent = null

                removingNode.leftChild!!.parent = removingNode.parent
                removingNode.parent!!.whichChild(removingNode).set(removingNode.leftChild)

                insertNode(child)
            }
        }

        return true
    }

    override fun insert(key: KeyT, value: ValueT): Boolean {
        val temp = findNodeOrPotentialParent(key) ?: run {
            root = ConsistentNode(key, value)
            return true
        }

        if (temp.key == key) return false
        else if (temp.key < key) temp.rightChild = ConsistentNode(key, value, temp)
        else temp.leftChild = ConsistentNode(key, value, temp)

        return true
    }

    private fun insertNode(node: ConsistentNode<KeyT, ValueT>): Boolean {
        val temp = findNodeOrPotentialParent(node.key)!!
        node.parent = temp
        temp.rightChild = node
        return true
    }

    /**
     * Finds potential parent of node key.
     * @param key key of searching node.
     * @return node, which can be a parent of node - if it doesn't exist, node with [key] - if it exists, *null* - if tree is empty.
     */
    private fun findNodeOrPotentialParent(key: KeyT): ConsistentNode<KeyT, ValueT>? {
        var temp = root ?: return null
        var isNeededNode = temp.key.compareTo(key)

        while (isNeededNode != 0) {
            temp =
                if (isNeededNode > 0) temp.leftChild ?: return temp
                else temp.rightChild ?: return temp

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

            private fun treeTraversal(node: ConsistentNode<KeyT, ValueT>?, list: ArrayList<Pair<KeyT, ValueT>>) {
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
        if (other !is ConsistentTree<*, *>) return false
        if (this === other) return true
        val (thisElements, otherElements) =
            arrayOf(mutableListOf<Pair<*, *>>(), mutableListOf())
        this.iterator().forEach {
            thisElements.add(it)
        }
        (other).iterator().forEach {
            otherElements.add(it)
        }
        return thisElements == otherElements
    }

    override fun hashCode(): Int {
        val elements = mutableListOf<Pair<KeyT, ValueT>>()
        this.iterator().forEach {
            elements.add(it)
        }

        return elements.hashCode()
    }
}
