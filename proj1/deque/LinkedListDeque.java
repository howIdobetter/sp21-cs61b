package deque;

import java.util.Iterator;

/**
 * Implement a circular LinkedListDeque from scratch.
 *  @author Yuhao Wang
 */

public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {
    /** A linked list deque need item, prev, next, size,
     * sentinel, and the last node's next is sentinel's
     * next.
     **/
    private class IntNode {
        T item;
        IntNode next;
        IntNode prev;

        public IntNode(T item, IntNode next, IntNode prev) {
            this.item = item;
            this.next = next;
            this.prev = prev;
        }
    }

    private IntNode sentinel;
    private int size;

    /** Create an empty linked list deque */
    public LinkedListDeque() {
        sentinel = new IntNode(null, null, null);
        size = 0;
    }

    /** Adds an item of type T to the front of the deque.
     * You can assume that item is never null.
     */
    @Override
    public void addFirst(T item) {
        size += 1;
        if (sentinel.next == null) {
            sentinel.next = new IntNode(item, null, null);
            sentinel.next.next = sentinel.next;
            sentinel.next.prev = sentinel.next;
        } else {
            IntNode newNode = new IntNode(item, sentinel.next, sentinel.next.prev);
            sentinel.next.prev.next = newNode;
            sentinel.next.prev = newNode;
            sentinel.next = newNode;
        }
    }

    /** Adds an item of type T to the back of the deque.
     * You can assume that item is never null.
     */
    @Override
    public void addLast(T item) {
        size += 1;
        if (sentinel.next == null) {
            sentinel.next = new IntNode(item, null, null);
            sentinel.next.next = sentinel.next;
            sentinel.next.prev = sentinel.next;
        } else {
            IntNode newNode = new IntNode(item, sentinel.next, sentinel.next.prev);
            sentinel.next.prev.next = newNode;
            sentinel.next.prev = newNode;
        }
    }

    /** Returns the number of items in the deque. */
    @Override
    public int size() {
        return this.size;
    }

    /** Prints the items in the deque from first to last,
     *  separated by a space. Once all the items have
     *  been printed, print out a new line.
     **/
    @Override
    public void printDeque() {
        IntNode start = this.sentinel.next;
        if (start != null) {
            System.out.print(start.item);
            System.out.print(" ");
            start = start.next;
            while (start != sentinel.next) {
                System.out.print(start.item);
                if (start.next != sentinel) { System.out.print(" "); };
                start = start.next;
            }
        }
        System.out.println();
    }

    /** Removes and returns the item at the front
     *  of the deque. If no such item exists, returns null.
     **/
    @Override
    public T removeFirst() {
        if (sentinel.next == null) {
            return null;
        } else if (sentinel.next.next == sentinel.next) {
            size -= 1;
            T x = sentinel.next.item;
            sentinel.next = null;
            return x;
        } else {
            size -= 1;
            T x = sentinel.next.item;
            sentinel.next.next.prev = sentinel.next.prev;
            sentinel.next.prev.next = sentinel.next.next;
            sentinel.next = sentinel.next.next;
            return x;
        }
    }

    /** Removes and returns the item at the back
     * of the deque. If no such item exists, returns null.
     **/
    @Override
    public T removeLast() {
        if (sentinel.next == null) {
            return null;
        } else if (sentinel.next.next == sentinel.next) {
            T x = sentinel.next.item;
            size -= 1;
            sentinel.next = null;
            return x;
        } else {
            size -= 1;
            T x = sentinel.next.prev.item;
            sentinel.next.prev.prev.next = sentinel.next;
            sentinel.next.prev = sentinel.next.prev.prev;
            return x;
        }
    }

    /** Gets the item at the given index, where 0 is the front,
     * 1 is the next item, and so forth. If no such item exists,
     * returns null. Must not alter the deque!
     **/
    @Override
    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        IntNode start = this.sentinel.next;
        while (index > 0) {
            start = start.next;
            index -= 1;
        }
        return start.item;
    }

    /** Same as get, but uses recursion. */
    public T getRecursive(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        return getRecursiveCombination(sentinel.next, index);
    }

    private T getRecursiveCombination(IntNode start, int index) {
        if (index == 0) {
            return start.item;
        }
        return getRecursiveCombination(start.next, index - 1);
    }

    /** The Deque objects we’ll make are iterable
     * (i.e. Iterable<T>) so we must provide this
     * method to return an iterator.
     **/
    @Override
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    private class LinkedListIterator implements Iterator<T> {
        private int wizPos;
        public LinkedListIterator() {
            wizPos = 0;
        }

        @Override
        public boolean hasNext() {
            return wizPos < size;
        }

        @Override
        public T next() {
            T returnItem = get(wizPos);
            wizPos += 1;
            return returnItem;
        }
    }

    /** Returns whether or not the parameter o is equal to the
     * Deque. o is considered equal if it is a Deque and if
     * it contains the same contents (as goverened by the generic
     * T’s equals method) in the same order. (ADDED 2/12:
     * You’ll need to use the instance of keywords for this.
     * Read here for more information)
     **/
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other.getClass() != this.getClass()) {
            return false;
        }
        LinkedListDeque<T> o = (LinkedListDeque<T>) other;
        if (o.size() != this.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!this.get(i).equals(o.get(i))) {
                return false;
            }
        }
        return true;
    }
}
