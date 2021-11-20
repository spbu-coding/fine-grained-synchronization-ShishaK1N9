package utils

/**
 * Tree interface.
 * @property KeyT nodes key type.
 * @property ValueT nodes value type.
 */
interface ITree<KeyT : Comparable<KeyT>, ValueT> {

    /**
     * Searches nodes value via key.
     * @param key key by which value is searched.
     * @return node value - if it exists, else - *null*.
     */
    fun search(key: KeyT): ValueT?

    /**
     * Removes node via key.
     * @param key key by which node is removed.
     * @return *true* - if node exists, *false* - if node doesn't exist.
     */
    fun remove(key: KeyT): Boolean

    /**
     * Inserts node to the tree.
     * @param key inserting node key.
     * @param value inserting node value.
     * @return *true* - if inserting node doesn't exist in the tree, *false* - if inserting node already exists in the tree.
     */
    fun insert(key: KeyT, value: ValueT): Boolean
}