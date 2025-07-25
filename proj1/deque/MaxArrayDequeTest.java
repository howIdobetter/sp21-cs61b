package deque;

/**
 *  use a test for MaxArrayDeque;
 *  @author Yuhao Wang
 */

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import java.util.Comparator;

public class MaxArrayDequeTest {
    @Test
    public void test1() {
        Cmp1 cmp = new Cmp1();
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(cmp);
        int N = 500;
        int expected = -1;
        for (int i = 0; i < N; i++) {
            int operationNumber = StdRandom.uniform(0, 2);
            if (operationNumber == 0) {
                int randVal =  StdRandom.uniform(0, 100);
                expected = Math.max(expected, randVal);
                mad.addLast(randVal);
            } else if (operationNumber == 1) {
                int randVal =  StdRandom.uniform(0, 100);
                expected = Math.max(expected, randVal);
                mad.addFirst(randVal);
            }
        }
        int input = mad.max();
        org.junit.Assert.assertEquals("The two numbers: expected are unequal!", expected, input);
    }

    @Test
    public void test2() {
        Cmp2 cmp = new Cmp2();
        MaxArrayDeque<String> mad =  new MaxArrayDeque<>(cmp);
        mad.addLast("a");
        mad.addLast("b");
        mad.addLast("c");
        String expected = "a";
        String input = mad.max();
        org.junit.Assert.assertEquals("The two strings are unequal!", expected, input);
    }
}

