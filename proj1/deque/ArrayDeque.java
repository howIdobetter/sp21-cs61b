package deque;

/**
 * Implement a circular ArrayDeque from scratch.
 *  @author Yuhao Wang
 */

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T>, Deque<T> {
    private int size, nextFirst, nextLast, capacity;
    private T[] items;

    /**
     * Constructor for ArrayDeque
     */
    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextFirst = 7;
        nextLast = 0;
        capacity = 8;
    }

    /**
     * Private helper function for resizing the deque if capacity is not enough
     * or too much
     */
    private void resize(int cap) {
        T[] temp = (T[]) new Object[cap];
        int first = (nextFirst + 1) % capacity;
        int last = (nextLast - 1 + capacity) % capacity;
        if (first <= last) {
            System.arraycopy(items, first, temp, 0, size);
        } else {
            System.arraycopy(items, first, temp, 0, capacity - first);
            System.arraycopy(items, 0, temp, capacity - first, nextLast);
        }
        nextFirst = cap - 1;
        nextLast = size;
        capacity = cap;
        items = temp;
    }

    /**
     * Add an item to the front of the deque
     */
    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(2 * capacity);
        }
        items[nextFirst] = item;
        size++;
        nextFirst = (nextFirst - 1 + capacity) % capacity;
    }
    /**
     * Add an item to the back of the deque
     */
    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize(2 * capacity);
        }
        items[nextLast] = item;
        size++;
        nextLast = (nextLast + 1) % capacity;
    }

    /**
     * @return how many items in the deque
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Print the deque from first to last, separated from white space.
     * Once all the items have been printed, print out a new line.
     */
    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }

    /**
     * Removes and returns the item at the front of the deque.
     * If no such item exists, return null
     * @return removeItem or null
     */
    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        int first = nextFirst + 1 < capacity ? nextFirst + 1 : 0;
        T x = items[first];
        items[first] = null;
        nextFirst = first;
        size -= 1;
        if (capacity >= 16 && (double) size / capacity < 0.25) {
            resize(capacity / 2);
        }
        return x;
    }

    /**
     * Removes and returns the item at the back of the deque.
     * If no such item exists, return null
     * @return removeItem or null
     */
    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        int last =  nextLast - 1 < 0 ? capacity - 1 : nextLast - 1;
        T x = items[last];
        items[last] = null;
        nextLast = last;
        size -= 1;
        if (capacity >= 16 && (double) size / capacity < 0.25) {
            resize(capacity / 2);
        }
        return x;
    }

    /**
     * Gets the item at the given index, where 0 is the front,
     * 1 is the next item, and so forth.
     *
     * If no such item exists, returns null
     * @return item or null
     */
    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        int first = (nextFirst + 1) % capacity;
        return items[(first + index) % capacity];
    }

    /**
     * Make objects in the deque iterable
     * @return iterator
     */
    @Override
    public Iterator<T> iterator() {
        return new DequeIterator();
    }

    private class DequeIterator implements Iterator<T> {
        private int wizPos;
        DequeIterator() {
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

    /**
     * @return whether the two object are equal
     */

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
        ArrayDeque<T> o = (ArrayDeque<T>) other;
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
