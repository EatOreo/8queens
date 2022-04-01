import java.util.ArrayList;

import net.sf.javabdd.*;

//javac -cp .:javabdd-1.0b2.jar TheLogic.java 
//java -cp .:javabdd-1.0b2.jar Queens TheLogic 8

public class TheLogic implements IQueensLogic{
    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)
    private BDDFactory fact = JFactory.init(2000000,200000);
    private int[][] varMap;
    private BDD bdd;

    public void initializeBoard(int size) {
        this.size = size;
        this.board = new int[size][size];
        varMap = new int[size][size];
        int k = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                //each square is assigned a variable id
                varMap[i][j] = k;
                k++;
            }
        }
        initBDD(size);
        updateBoard(); //in case there are any restrictions by default (there are not though)
    }
   
    public int[][] getBoard() {
        return board;
    }

    public void insertQueen(int column, int row) {
        if (board[column][row] != 0) return; //insert is denied if square has been determined
        var v = varAt(column, row);
        bdd = bdd.restrict(v); //v (variable representing the square) is restricted to be true
        board[column][row] = 1; //queen is inserted
        updateBoard();
    }

    private void updateBoard() {
        //iterate over each square
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 1 || board[i][j] == -1) continue;
                var v = varAt(i, j); //variable for the square
                if (v.isZero() || bdd.restrict(v).isZero()) {
                    //if when v is set to true, the bdd turns false, the square must be false
                    board[i][j] = -1; //thus no queen
                }
                else if (v.isOne() || bdd.restrict(v.not()).isZero()) {
                    //if when v is set to false, the bdd turns false, the square must be true
                    board[i][j] = 1; //thus queen
                } 
            }
        }
    }

    private BDD varAt(int x, int y) {
        return fact.ithVar(varMap[x][y]);
    }

    private void initBDD(int n) {
        fact.setVarNum(n * n);
        bdd = fact.one();

        //this is the list for all sets of lines that block each other
        var lines = new ArrayList<ArrayList<BDD>>();

        //column and row lines are added
        for (int i = 0; i < n; i++) {
            //vv these are for defining there must be one queen on each column + row
            var xBDD = fact.zero();
            var yBDD = fact.zero();

            var xl = new ArrayList<BDD>();
            var yl = new ArrayList<BDD>();
            for (int j = 0; j < n; j++) {
                var x = varAt(i, j);
                var y = varAt(j, i);

                xl.add(x);
                yl.add(y);

                //(x, 0) or (x, 1) or (x, 2)...
                xBDD = xBDD.or(x);
                yBDD = yBDD.or(y);
            }
            lines.add(xl);
            lines.add(yl);

            bdd = bdd.and(xBDD);
            bdd = bdd.and(yBDD);
        }

        //diagonals are added
        //https://stackoverflow.com/questions/20420065/loop-diagonally-through-two-dimensional-array
        for(int k = 0; k < n * 2; k++ ) {
            var xl = new ArrayList<BDD>();
            var yl = new ArrayList<BDD>();
            for( int j = 0; j <= k; j++ ) {
                int i = k - j;
                if( i < n && j < n ) {
                    xl.add(varAt(i, j));
                    yl.add(varAt(n - i - 1, j));
                }
                lines.add(xl);
                lines.add(yl);
            }
        }

        //for each line no two variables can be true at the same time
        for (ArrayList<BDD> line : lines) {
            for (int i = 0; i < line.size(); i++) {
                var a = line.get(i);
                for (int j = i + 1; j < line.size(); j++) {
                    var b = line.get(j);
                    // (not ((0, 0) and (0, 1))) and (not ((0, 0) and (0, 2)))...
                    bdd = bdd.and(a.and(b).not());
                }
            }
        }
    }

    // vvv For debugging vvv

    public static void main(String[] args) {
        
        var n = 8;
        var logic = new TheLogic();
        logic.initializeBoard(n);

        //logic.printBDD();

        logic.insertQueen(1, 1);
        logic.insertQueen(6, 5);

        logic.printBoard();
    }

    public void printBoard() {
        var signs = new String[]{"x", "-", "Q"};

        var b = getBoard();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(signs[b[i][j] + 1] + " ");
            }
            System.out.println();
        }
    }

    public void printBDD() {
        fact.printTable(bdd);
    }
}