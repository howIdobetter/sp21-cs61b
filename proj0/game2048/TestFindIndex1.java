package game2048;

import org.junit.Assert;
import org.junit.Test;

public class TestFindIndex1 {
    @Test
    public void test() {
        int[][] board = new int[][]{
                {2, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };
        Board b = new Board(board, 0);
        int num = Model.findIndex1(0, b);
        Assert.assertEquals(3, num);
    }

    @Test
    public void test2() {
        int[][] board = new int[][]{
                {2, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 0, 4, 0},
                {0, 0, 0, 4},
        };
        Board b = new Board(board, 0);
        int num0 =  Model.findIndex1(0, b);
        Assert.assertEquals(3, num0);
        int num1 =  Model.findIndex1(1, b);
        Assert.assertEquals(2, num1);
        int num2 =  Model.findIndex1(2, b);
        Assert.assertEquals(1, num2);
        int num3 =   Model.findIndex1(3, b);
        Assert.assertEquals(0, num3);
    }
}
