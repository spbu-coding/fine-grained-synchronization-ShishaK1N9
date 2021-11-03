package binary_tree

open class Tree<KeyT : Comparable<KeyT>, ValueT> {

    private var root: Node<KeyT, ValueT>? = null

    fun search(key: KeyT): ValueT? {
        val temp = findNodeOrPotentialParent(key) ?: return null
        return if (temp.key == key) temp.value else null
    }

    fun remove(key: KeyT): Boolean {
        val removingNode = findNodeOrPotentialParent(key) ?: return false

        if (removingNode.key != key) return false

        if (removingNode == root) {
            when (root!!.countOfChildren()) {
                0 -> root = null
                1 -> {
                    root = root!!.leftChild ?: root!!.rightChild
                    root!!.parent = null
                }
                2 -> {
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
                removingNode.parent!!.whichChild(removingNode).set(removingNodeChild)
                removingNodeChild!!.parent!!.whichChild(removingNodeChild).set(removingNode.parent)
            }
            2 -> {
                val child = removingNode.rightChild!!
                child.parent = null

                removingNode.leftChild!!.parent = removingNode.parent
                removingNode.parent!!.whichChild(removingNode).set(removingNode.leftChild)

                insertNode(child)
            }
        }

        return true
    }

    fun insert(key: KeyT, value: ValueT): Boolean {
        val temp = findNodeOrPotentialParent(key) ?: run {
            root = Node(key, value)
            return true
        }

        if (temp.key == key) return false
        else if (temp.key < key) {
            temp.rightChild = Node(key, value)
            temp.rightChild!!.parent = temp
        } else {
            temp.leftChild = Node(key, value)
            temp.leftChild!!.parent = temp
        }
        return true
    }

    private fun insertNode(node: Node<KeyT, ValueT>): Boolean {
        val temp = findNodeOrPotentialParent(node.key) ?: run {
            root = node
            return true
        }

        if (temp.key < node.key) {
            node.parent = temp
            temp.rightChild = node
        } else {
            node.parent = temp
            temp.leftChild = node
        }
        return true
    }

    private fun findNodeOrPotentialParent(key: KeyT): Node<KeyT, ValueT>? {
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

            private fun treeTraversal(node: Node<KeyT, ValueT>?, list: ArrayList<Pair<KeyT, ValueT>>) {
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
        val otherIterator = (other as Tree<*, *>).iterator()
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
        fun calculateArrayStringRepresentation(node: Node<KeyT, ValueT>?, list: ArrayList<String>) {
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
