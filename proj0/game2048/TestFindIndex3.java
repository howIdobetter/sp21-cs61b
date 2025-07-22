package game2048;

import org.junit.Assert;
import org.junit.Test;

public class TestFindIndex3 {
    @Test
    public void test() {
        int[][] board = new int[][]{
                {2, 0, 0, 0},
                {0, 0, 0, 0},
                {2, 0, 0, 0},
                {2, 0, 0, 0},
        };
        Board b = new Board(board, 0);
        int[] num = Model.findIndex3(0, b);
        int[] result = new int[] {0, 1, 3};
        Assert.assertArrayEquals(result, num);
    }

    @Test
    public void test2() {
        int[][] board = new int[][]{
                {2, 0, 4, 0},
                {2, 2, 2, 2},
                {4, 4, 4, 2},
                {0, 2, 0, 4},
        };
        Board b = new Board(board, 0);
        int[] num0 =  Model.findIndex3(0, b);
        int[] result0 = new int[] {1, 2, 3};
        Assert.assertArrayEquals(result0, num0);
        int[] num1 =  Model.findIndex3(1, b);
        int [] result1 = new int[] {0, 1, 2};
        Assert.assertArrayEquals(result1, num1);
        int[] num2 =  Model.findIndex3(2, b);
        int[] result2 = new int[] {1, 2, 3};
        Assert.assertArrayEquals(result2, num2);
        int[] num3 =   Model.findIndex3(3, b);
        int[]  result3 = new int[] {0, 1, 2};
        Assert.assertArrayEquals(result3, num3);
    }
}
