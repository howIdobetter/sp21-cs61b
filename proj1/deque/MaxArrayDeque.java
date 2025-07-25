package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> cmp;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        cmp = c;
    }

    public T max() {
        return max(cmp);
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }

        T maxItem = this.get(0);
        for (int i = 0; i < size(); i++) {
            if (c.compare(this.get(i), maxItem) > 0) {
                maxItem = get(i);
            }
        }
        return maxItem;
    }
}
