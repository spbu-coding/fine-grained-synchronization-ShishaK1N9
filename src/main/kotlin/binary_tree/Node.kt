package binary_tree

internal class Node<KeyT : Comparable<KeyT>, ValueT>(var key: KeyT, var value: ValueT) {

    var parent: Node<KeyT, ValueT>? = null
    var leftChild: Node<KeyT, ValueT>? = null
    var rightChild: Node<KeyT, ValueT>? = null

    internal fun whichChild(node: Node<KeyT, ValueT>) =
        if (node.key == leftChild?.key) ::leftChild
        else ::rightChild

    internal fun countOfChildren() =
        (leftChild?.let { 1 } ?: 0) + (rightChild?.let { 1 } ?: 0)
}