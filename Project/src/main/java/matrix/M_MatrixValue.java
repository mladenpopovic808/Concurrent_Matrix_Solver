package matrix;

import java.math.BigInteger;

public class M_MatrixValue {

    public final int row;
    public final int col;
    public final BigInteger value;

    public M_MatrixValue(int row, int col, BigInteger value) {
        this.row = row;
        this.col = col;
        this.value = value;
    }
}
