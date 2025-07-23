package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;

public class RandomizedTest {
    public static void main(String[] args) {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> L1 = new BuggyAList<>();
        for (int i = 0; i < 5000; i++) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                L1.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size1 = L.size();
                int size2 = L1.size();
                org.junit.Assert.assertEquals(size1, size2);
                System.out.println("size1: " + size1 + " size2: " + size2);
            } else if (operationNumber == 2 && L.size() > 0) {
                int randVal = L.getLast();
                int randVal1 = L1.getLast();
                org.junit.Assert.assertEquals(randVal1, randVal);
                System.out.println("L.getLast(" + randVal + ")" + "L1.getLast(" + randVal + ")");
            } else if (operationNumber == 3 && L.size() > 0) {
                int randVal = L.removeLast();
                int randVal1 = L1.removeLast();
                org.junit.Assert.assertEquals(randVal1, randVal);
                System.out.println("L.removeLast: " + randVal + "  L1.getLast: " + randVal);
            }
        }
    }
}
