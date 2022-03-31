import net.sf.javabdd.*;

//javac -cp .:javabdd-1.0b2.jar TheLogic.java 
//java -cp .:javabdd-1.0b2.jar Queens TheLogic 8

public class TheLogic implements IQueensLogic{
    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)
    private BDDFactory fact = JFactory.init(20,20);

    public void initializeBoard(int size) {
        this.size = size;
        this.board = new int[size][size];
        fact.setVarNum(size * size);
    }
   
    public int[][] getBoard() {
        return board;
    }

    public void insertQueen(int column, int row) {
        board[column][row] = 1;
    }    
}
