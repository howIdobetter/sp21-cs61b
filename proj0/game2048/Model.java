package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author TODO: YOUR NAME HERE
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.

        board.setViewingPerspective(side);
        /* only up the tile*/
        if (judgeChange(board)) {
            changed = true;
            /**    We just need deal with each cow and one row has ___ situation
             *  1. Empty: skip;
             *  2. One tile: find this tile and move it to the top;
             *  3. Two tiles: We must judge if the two tiles equal:
             *  (1) Yes? We move the two tiles to the top;
             *  (2) No? We move the two tiles to the top and the less top;
             *  4. Three tiles: We must judge if every two adjacent tiles equal:
             *  (1) Yes? We move them to the corresponding place, it has two situations:
             *  1) row 0 and row 1
             *  2) row 1 and row 2
             *  But carefully, we just use the size of row instead of the true name of row.
             *  The priority:
             *  2) > 1)
             *  (2) NO? It's straightforward;
             *  5. Five tiles: We must judge:
             *  (1) Yes? It has 4 situations:
             *  1) row 0 and row 1
             *  2) row 2 and row 3
             *  3) row 0 and roe 1, row 2 and row 3
             *  4) row 1 and row 2
             *  We must deal with the priorities:
             *  2) > 3) > 1)
             *  The priorities decide our order to deal with this message.
             **/
            for (int col = 0; col < board.size(); col += 1) {
                int tile_num = tileNum(col, board);
                switch (tile_num) {
                    case 0:
                        // Just skip
                        break;
                    case 1:
                        // Move to the top if it's not at the top.
                        int operate_index1 = findIndex1(col, board);
                        Tile t = board.tile(col, operate_index1);
                        board.move(col, 3, t);
                        break;
                    case 2:
                        int[] operate_index2 = findIndex2(col, board);
                        Tile t_2_0 =  board.tile(col, operate_index2[0]);
                        Tile t_2_1 = board.tile(col, operate_index2[1]);
                        if (t_2_0.value() == t_2_1.value()) {
                            int add_value = t_2_0.value() * 2;
                            score += add_value;
                            board.move(col, 3, t_2_1);
                            board.move(col, 3, t_2_0);
                        } else {
                            board.move(col, 3, t_2_1);
                            board.move(col, 2, t_2_0);
                        }
                        break;
                    case 3:
                        int[] operate_index3 = findIndex3(col, board);
                        Tile t_3_0 = board.tile(col, operate_index3[0]);
                        Tile t_3_1 = board.tile(col, operate_index3[1]);
                        Tile t_3_2 = board.tile(col, operate_index3[2]);
                        if (t_3_2.value() == t_3_1.value()) {
                            score += t_3_1.value() * 2;
                            board.move(col, 3, t_3_2);
                            board.move(col, 3, t_3_1);
                            board.move(col, 2, t_3_0);
                        } else if (t_3_1.value() == t_3_0.value()) {
                            score += t_3_1.value() * 2;
                            board.move(col, 3, t_3_2);
                            board.move(col, 2, t_3_1);
                            board.move(col, 2, t_3_0);
                        } else {
                            board.move(col, 3, t_3_2);
                            board.move(col, 2, t_3_1);
                            board.move(col, 1, t_3_0);
                        }
                        break;
                    default:
                        Tile t_4_0 = board.tile(col, 0);
                        Tile t_4_1 = board.tile(col, 1);
                        Tile t_4_2 = board.tile(col, 2);
                        Tile t_4_3 = board.tile(col, 3);
                        if (t_4_3.value() == t_4_2.value()) {
                            score += t_4_2.value() * 2;
                            board.move(col, 3, t_4_2);
                            if (t_4_1.value() == t_4_0.value()) {
                                score += t_4_1.value() * 2;
                                board.move(col, 2, t_4_1);
                                board.move(col, 2, t_4_0);
                            } else {
                                board.move(col, 2, t_4_1);
                                board.move(col, 1, t_4_0);
                            }
                        } else if (t_4_2.value() == t_4_1.value()) {
                            score += t_4_1.value() * 2;
                            board.move(col, 2, t_4_1);
                            board.move(col, 1, t_4_0);
                        } else if (t_4_1.value() == t_4_0.value()) {
                            score += t_4_1.value() * 2;
                            board.move(col, 1, t_4_0);
                        } else {
                            break;
                        }
                        break;
                }
            }
        }
        board.setViewingPerspective(Side.NORTH);
        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    /** return if the board can change in the side */
    public static boolean judgeChange(Board b) {
        for (int col = 0; col < b.size(); col ++) {
            if (colAtLeastOneMoveExists(col, b)) {
                return true;
            }
        }
        return false;
    }

    /** return each col */
    public static boolean colAtLeastOneMoveExists(int col, Board b) {
        for (int row = 3; row > 0; row --) {
            if (b.tile(col, row) == null) {
                for (int r = row - 1; r >= 0; r --) {
                    if (b.tile(col, r) != null) {
                        return true;
                    }
                }
            }
        }
        for (int row = 3; row > 0; row --) {
            if (b.tile(col, row) != null) {
                if (b.tile(col, row - 1) != null) {
                    if (b.tile(col, row).value() == b.tile(col, row - 1).value()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** return the number of tiles in some column
     */
    public static int tileNum(int column, Board b) {
        int count = 0;
        for (int row = 0; row < b.size(); row ++) {
            if (b.tile(column, row) != null) {
                count ++;
            }
        }
        return count;
    }

    /** return the index of tile whose column has just one tile
     */

    public static int findIndex1(int column, Board b) {
        int result = 0;
        for (int row = 0; row < b.size(); row ++) {
            if (b.tile(column, row) != null) {
                result = row;
            }
        }
        return result;
    }

    /** return the index of tile whose column has just two tiles
     */
    public static int[] findIndex2(int column, Board b) {
        int[] result = new int[2];
        int cnt = 0;
        for (int row = 0; row < b.size(); row ++) {
            if (b.tile(column, row) != null) {
                result[cnt ++] = row;
            }
        }
        return result;
    }

    /** return the index of tile whose column has just three tiles
     */
    public static int[] findIndex3(int column, Board b) {
        int[] result = new int[3];
        int cnt = 0;
        for (int row = 0; row < b.size(); row ++) {
            if (b.tile(column, row) != null) {
                result[cnt++] = row;
            }
        }
        return result;
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.
        for (int i = 0; i < 4; i ++) {
            for (int j = 0; j < 4; j ++) {
                if (b.tile(i, j) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.
        for (int i = 0; i < 4; i ++) {
            for (int j = 0; j < 4; j ++) {
                if (b.tile(i, j) == null) {
                    continue;
                }
                int value = b.tile(i, j).value();
                if (value == MAX_PIECE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        /* situation 1 */
        for (int i = 0; i < 4; i ++) {
            for (int j = 0; j < 4; j ++) {
                if (b.tile(i, j) == null) {
                    return true;
                }
            }
        }

        /* situation 2 */
        /* row */
        for (int i = 0; i < 4; i ++) {
            for (int j = 0; j < 3; j ++) {
                if (b.tile(i, j).value() == b.tile(i, j + 1).value()) {
                    return true;
                }
            }
        }

        for (int j = 0; j < 4; j ++) {
            for (int i = 0; i < 3; i ++) {
                if (b.tile(i, j).value() == b.tile(i + 1, j).value()) {
                    return true;
                }
            }
        }

        /* default */
        return false;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
