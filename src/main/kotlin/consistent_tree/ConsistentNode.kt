package consistent_tree

/**
 * Consistent node implementation.
 * @property key node key
 * @property value node value
 * @property parent node parent (default value is *null*)
 */
internal class ConsistentNode<KeyT : Comparable<KeyT>, ValueT>(
    initialKey: KeyT,
    initialValue: ValueT,
    var parent: ConsistentNode<KeyT, ValueT>? = null
) {

    var key = initialKey
        private set
    var value = initialValue
        private set

    var leftChild: ConsistentNode<KeyT, ValueT>? = null
    var rightChild: ConsistentNode<KeyT, ValueT>? = null

    /**
     * Detects which child the node is.
     * @param node child which needed to be identified.
     * @return [leftChild] link - if [node] is [leftChild], [rightChild] link - if node is [rightChild].
     */
    internal fun whichChild(node: ConsistentNode<KeyT, ValueT>) =
        if (node.key == leftChild?.key) ::leftChild
        else ::rightChild

    /**
     * Count children of current node.
     * @return 0 - if node is a leave, 1 - if node has only one child and 2 - if node has two children.
     */
    internal fun countOfChildren() =
        (leftChild?.let { 1 } ?: 0) + (rightChild?.let { 1 } ?: 0)
}