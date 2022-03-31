import java.util.ArrayList;
import java.util.HashSet;

import net.sf.javabdd.*;

//javac -cp .:javabdd-1.0b2.jar TheLogic.java 
//java -cp .:javabdd-1.0b2.jar Queens TheLogic 8

public class TheLogic implements IQueensLogic{
    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)
    private BDDFactory fact = JFactory.init(2000000,200000);
    private int[][] varMap;
    private BDD bdd;

    public static void main(String[] args) {
        var logic = new TheLogic();
        logic.initializeBoard(2);

        logic.insertQueen(0, 0);
    }

    public void initializeBoard(int size) {
        this.size = size;
        this.board = new int[size][size];
        varMap = new int[size][size];
        int k = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                varMap[i][j] = k;
                k++;
            }
        }
        initBDD(size);
        
    }
   
    public int[][] getBoard() {
        return board;
    }

    public void insertQueen(int column, int row) {
        board[column][row] = 1;
        int v = varAt(column, row);
        fact.printTable(bdd.restrict(fact.ithVar(v)));
    }

    private int varAt(int x, int y) {
        return varMap[x][y];
    }

    private void initBDD(int n) {
        fact.setVarNum(n * n);
        bdd = fact.one();

        //vertical and horizontal lines
        for (int i = 0; i < n; i++) {
            var xBDD = fact.zero();
            var yBDD = fact.zero();
            for (int j = 0; j < n; j++) {
                xBDD = xBDD.xor(fact.ithVar(varAt(i, j)));
                yBDD = yBDD.xor(fact.ithVar(varAt(j, i)));
            }
            bdd = bdd.and(xBDD).and(yBDD);
        }

        // //diagonals
        // var dias = new ArrayList<HashSet<Integer>>();
        // for(int k = 0; k < n * 2; k++ ) {
        //     dias.add(new HashSet<>());
        //     for( int j = 0; j <= k; j++ ) {
        //         int i = k - j;
        //         if( i < n && j < n ) {
        //             dias.get(k).add(varAt(i, j));
        //         }
        //     }
        // }
        // for(int k = 0; k < n * 2; k++ ) {
        //     dias.add(new HashSet<>());
        //     for( int j = 0; j <= k; j++ ) {
        //         int i = k - j;
        //         if( i < n && j < n ) {
        //             dias.get(k + (n * 2)).add(varAt(n - i - 1, n - j - 1));
        //         }
        //     }
        // }

        // for (HashSet<Integer> s : dias) {
        //     var kBDD = fact.zero();
        //     for (var d : s) {
        //         kBDD = kBDD.xor(fact.ithVar(d));
        //     }
        //     bdd = bdd.and(kBDD);
        // }
    }
}

// old vertical and horizontal lines
// for (int i = 0; i < n; i++) {
//     var xBDD = fact.zero();
//     var yBDD = fact.zero();
//     for (int j = 0; j < n; j++) {
//         int k = varAt(i, j);
//         var kxBDD = fact.ithVar(k);
//         var kyBDD = fact.ithVar(k);
//         for (int l = 0; l < n; l++) {
//             if (l != j) {
//                 int x = varAt(i, l);
//                 kxBDD = kxBDD.and(fact.nithVar(x));
//             }
//             if (l != i) {
//                 int x = varAt(l, j);
//                 kyBDD = kyBDD.and(fact.nithVar(x));
//             }
//         }
//         xBDD = xBDD.or(kxBDD);
//         yBDD = yBDD.or(kyBDD);
//     }
//     bdd = bdd.and(xBDD).and(yBDD);
// }