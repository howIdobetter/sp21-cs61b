package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private int size;
    private int initialSize;
    private final double maxLoad;

    /** Constructors */
    public MyHashMap() {
        this.initialSize = 16;
        this.maxLoad = 0.75;
        this.buckets = createTable(this.initialSize);
        this.size = 0;
    }

    public MyHashMap(int initialSize) {
        this.initialSize = initialSize;
        this.maxLoad = 0.75;
        this.buckets = createTable(this.initialSize);
        this.size = 0;
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.initialSize = initialSize;
        this.maxLoad = maxLoad;
        this.buckets = createTable(this.initialSize);
        this.size = 0;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] buckets = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            buckets[i] = createBucket();
        }
        return buckets;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    private void resize(int capicity) {
        MyHashMap<K, V> temp = new MyHashMap<>(capicity, this.maxLoad);
        for (int i = 0; i < this.initialSize; i++) {
            for (Node node : this.buckets[i]) {
                temp.put(node.key, node.value);
            }
        }
        this.buckets = temp.buckets;
        this.size = temp.size;
        this.initialSize = temp.initialSize;
    }

    /* some interesting ways to get hash vaue;
    private int hashTextbook(K key) {
        return (key.hashCode() & 0x7FFFFFFF) % this.initialSize;
    }

    private int hash(K key) {
        int h = key.hashCode();
        h ^= (h >>> 20) ^ (h >>> 12) ^ (h >>> 7) ^ (h >>> 4);
        return h & (this.initialSize - 1);
    }
     */

    private int hash(K key) {
        int h = key.hashCode();
        return Math.floorMod(h, initialSize);
    }

    @Override
    public void clear() {
         size = 0;
         for (int i = 0; i < initialSize; i++) {
             if (buckets[i] != null) {
                 buckets[i].clear();
             }
         }
    }

    @Override
    public boolean containsKey(K key) {
        int hashValue = hash(key);
        Collection<Node> bucket = buckets[hashValue];
        for (Node node : bucket) {
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        int hashValue = hash(key);
        Collection<Node> bucket = buckets[hashValue];
        if (bucket != null) {
            for (Node node : bucket) {
                if (node.key.equals(key)) {
                    return node.value;
                }
            }
        }
        return null;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void put(K key, V value) {
        if ((double) size / initialSize > maxLoad) {
            resize(initialSize * 2);
        }
        int hashValue = hash(key);
        Collection<Node> bucket = buckets[hashValue];
        if (buckets[hashValue] != null) {
            for (Node node : bucket) {
                if (node.key.equals(key)) {
                    node.value = value;
                    return;
                }
            }
        }
        buckets[hashValue].add(new Node(key, value));
        size++;
    }

    @Override
    public Set<K> keySet() {
        Set<K> result =  new HashSet<>();
        for (int i = 0; i < initialSize; i++) {
            for (Node node : buckets[i]) {
                result.add(node.key);
            }
        }
        return result;
    }

    @Override
    public V remove(K key) {
        throw  new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw  new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        Set<K> result = keySet();
        return result.iterator();
    }

}
