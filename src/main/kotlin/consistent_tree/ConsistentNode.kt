package consistent_tree

internal class ConsistentNode<KeyT : Comparable<KeyT>, ValueT>(var key: KeyT, var value: ValueT) {

    var parent: ConsistentNode<KeyT, ValueT>? = null
    var leftChild: ConsistentNode<KeyT, ValueT>? = null
    var rightChild: ConsistentNode<KeyT, ValueT>? = null

    internal fun whichChild(node: ConsistentNode<KeyT, ValueT>) =
        if (node.key == leftChild?.key) ::leftChild
        else ::rightChild

    internal fun countOfChildren() =
        (leftChild?.let { 1 } ?: 0) + (rightChild?.let { 1 } ?: 0)
}