package deque;

public interface Deque<T> {
    void addLast(T item);
    void addFirst(T item);
    int size();
    void printDeque();
    T removeFirst();
    T removeLast();
    T get(int index);
    boolean isEmpty();
}
