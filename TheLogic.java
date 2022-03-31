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
        
        var n = 8;
        var logic = new TheLogic();
        logic.initializeBoard(n);

        logic.insertQueen(3, 1);
        logic.insertQueen(5, 5);

        logic.printBoard();
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

    private void updateBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 1 || board[i][j] == -1) continue;
                var k = varAt(i, j);
                if (k.isZero() || bdd.restrict(k).isZero()) {
                    board[i][j] = -1;
                }
                else if (k.isOne() || bdd.restrict(k.not()).isZero()) {
                    board[i][j] = 1;
                } 
            }
        }
    }

    public void insertQueen(int column, int row) {
        if (board[column][row] != 0) return;
        var v = varAt(column, row);
        bdd = bdd.restrict(v);
        board[column][row] = 1;
        updateBoard();
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

    private BDD varAt(int x, int y) {
        return fact.ithVar(varMap[x][y]);
    }

    private void initBDD(int n) {
        fact.setVarNum(n * n);
        bdd = fact.one();

        //vertical and horizontal lines
        for (int i = 0; i < n; i++) {
            var xBDD = fact.zero();
            var yBDD = fact.zero();
            for (int j = 0; j < n; j++) {
                var k = varAt(i, j);
                var kxBDD = varAt(i, j);
                var kyBDD = varAt(i, j);
                for (int l = 0; l < n; l++) {
                    if (l != j) {
                        kxBDD = kxBDD.and(k.and(varAt(i, l)).not());
                    }
                    if (l != i) {
                        kyBDD = kyBDD.and(k.and(varAt(l, j)).not());
                    }
                }
                xBDD = xBDD.or(kxBDD);
                yBDD = yBDD.or(kyBDD);
            }
            bdd = bdd.and(xBDD).and(yBDD);
        }
        

        // //diagonals
        // var dias = new ArrayList<HashSet<BDD>>();
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

        // for (HashSet<BDD> s : dias) {
        //     var kBDD = fact.zero();
        //     for (var d : s) {
        //         kBDD = kBDD.xor(d);
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
//         // (true XOR true) XOR true = true
//         xBDD = xBDD.xor(varAt(i, j));
//         yBDD = yBDD.xor(varAt(j, i));
//     }
//     bdd = bdd.and(xBDD).and(yBDD);
// }