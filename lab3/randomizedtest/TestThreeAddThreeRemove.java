package randomizedtest;

import org.junit.Test;
import timingtest.AList;
import org.junit.Assert;

public class TestThreeAddThreeRemove {
    @Test
    public void testThreeAdd() {
        AListNoResizing<Integer> A = new AListNoResizing<>();
        BuggyAList<Integer> B = new BuggyAList<>();
        A.addLast(4);
        A.addLast(5);
        A.addLast(6);
        B.addLast(4);
        B.addLast(5);
        B.addLast(6);
        org.junit.Assert.assertEquals(A.removeLast(), B.removeLast());
        org.junit.Assert.assertEquals(A.removeLast(), B.removeLast());
        org.junit.Assert.assertEquals(A.removeLast(), B.removeLast());
    }
}
