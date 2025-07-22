package game2048;

import org.junit.Assert;
import org.junit.Test;

public class TestTileNum {
    @Test
    public void test() {
        int[][] board = new int[][]{
                {2, 0, 0, 0},
                {2, 0, 0, 0},
                {2, 0, 0, 0},
                {0, 0, 0, 0},
        };
        Board b = new Board(board, 0);
        int num = Model.tileNum(0, b);
        Assert.assertEquals(3, num);
    }

    @Test
    public void test2() {
        int[][] board = new int[][]{
                {2, 4, 2, 0},
                {4, 2, 4, 0},
                {2, 4, 2, 0},
                {4, 2, 4, 0},
        };
        Board b = new Board(board, 0);
        int num0 =  Model.tileNum(0, b);
        Assert.assertEquals(4, num0);
        int num1 =  Model.tileNum(1, b);
        Assert.assertEquals(4, num1);
        int num2 =  Model.tileNum(2, b);
        Assert.assertEquals(4, num2);
        int num3 =   Model.tileNum(3, b);
        Assert.assertEquals(0, num3);
    }
}
