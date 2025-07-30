package bstmap;

import java.util.Iterator;
import java.util.Set;

/** A data structure that uses a Binary Search Tree (BST) to store pairs of keys and values.
 *  Keys must be Comparable and appear at most once in the map, but values may appear multiple
 *  times. Key operations get(key), put(key, value), and containsKey(key) methods
 *  leverage the sorted nature of the BST for efficient average-case performance. The value
 *  associated with a key is the value from the last call to put with that key.
 *  Compared to a simple linked-list implementation, BSTMap offers significantly faster
 *  average-case lookups, insertions, and updates, typically achieving logarithmic time complexity
 *  due to its structured, hierarchical organization. However, if keys are inserted in a highly
 *  sorted order, the BST can degenerate into a list-like structure, leading to linear time complexity
 *  in the worst case. BSTMap also naturally supports retrieving keys in sorted order.
 *
 * @author Yuhao Wang
 * */

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{
    private BSTNode root;

    private class BSTNode {
        private K key;
        private V value;
        private BSTNode left, right;
        private int size;

        public BSTNode(K key, V value, int size) {
            this.key = key;
            this.value = value;
            this.size = size;
        }
    }

    public BSTMap() {
    }

    /** Removes all of the mappings from this map. */
    @Override
    public void clear() {
        root = clear(root);
    }

    private BSTNode clear(BSTNode x) {
        if (x == null) {
            return null;
        } else {
            x.left = clear(x.left);
            x.right = clear(x.right);
            x.size = 0;
            x = null;
            return x;
        }
    }

    /* Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key) {
        return containsKey(root, key);
    }

    private boolean containsKey(BSTNode x, K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        if  (x == null) {
            return false;
        }
        int cmp = key.compareTo(x.key);
        if (cmp == 0) {
            return true;
        } else if (cmp < 0) {
            return containsKey(x.left, key);
        } else {
            return containsKey(x.right, key);
        }
    }

    /* Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        return get(root, key);
    }

    private V get(BSTNode x, K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        if (x == null) {
            return null;
        }
        int cmp = key.compareTo(x.key);
        if (cmp == 0) {
            return x.value;
        } else if (cmp < 0) {
            return get(x.left, key);
        } else {
            return get(x.right, key);
        }
    }

    /* Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return size(root);
    }

    private int size(BSTNode x) {
        if (x == null) {
            return 0;
        }
        return x.size;
    }

    /* Associates the specified value with the specified key in this map. */
    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        root = put(root, key, value);
    }

    private BSTNode put(BSTNode x, K key, V value) {
        if (x == null) {
            return new BSTNode(key, value, 1);
        }
        int cmp =  key.compareTo(x.key);
        if (cmp < 0) {
            x.left = put(x.left, key, value);
        } else if (cmp > 0){
            x.right = put(x.right, key, value);
        } else {
            x.value = value;
        }
        x.size = size(x.left) + size(x.right) + 1;
        return x;
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw  new UnsupportedOperationException();
    }

    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(BSTNode x) {
        if (x == null) {
            return;
        }
        printInOrder(x.left);
        System.out.print("Key: " + x.key + " Value: " + x.value + " Size: " + x.size);
        printInOrder(x.right);
    }
}
