package utils

interface ITree<KeyT : Comparable<KeyT>, ValueT> {

    fun search(key: KeyT): ValueT?
    fun remove(key: KeyT): Boolean
    fun insert(key: KeyT, value: ValueT): Boolean
}