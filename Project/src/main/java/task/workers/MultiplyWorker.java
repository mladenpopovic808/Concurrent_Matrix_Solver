package task.workers;

import matrix.M_Matrix;

import java.math.BigInteger;
import java.util.List;


public class MultiplyWorker implements Runnable {

    private final M_Matrix newMatrix;
    private final List<List<BigInteger>> rowList;
    private final int rowToBeginFrom;
    private final M_Matrix matrix2;

    public MultiplyWorker(M_Matrix newMatrix, List<List<BigInteger>> rowList, M_Matrix matrix2, int rowToBeginFrom) {
        this.newMatrix = newMatrix;
        this.rowList = rowList;
        this.matrix2 = matrix2;
        this.rowToBeginFrom = rowToBeginFrom;


    }

    @Override
    public void run() {


        //Problem je sto su vrednosti null,kao da nisam dobro setovao nule kada extractujem
        //Racunamo od reda koji nam je pocetak, naprez za velicinu liste redova prve matrice
        int end = rowToBeginFrom + rowList.size();
        //System.out.println(end);



        //Za svaki red nove matrice
        for (int i = rowToBeginFrom; i < end; i++) {

            //Za svaku kolonu nove matrice
            for (int j = 0; j < newMatrix.getCols(); j++) {


                BigInteger sum = BigInteger.ZERO;


                //mnozimo i sabiramo
                for (int k = 0; k < rowList.get(i-rowToBeginFrom).size(); k++) {

                    try{
                    sum = sum.add(rowList.get(i-rowToBeginFrom).get(k).multiply(matrix2.get(k, j)));


                    }catch (Exception e){ //debagovanje

                        e.printStackTrace();
                        //System.out.println(k+" RED"+matrix2.getRow(k));
                        //System.out.println("error za "+(i-rowToBeginFrom)+" "+k+" "+rowList.get(i-rowToBeginFrom).get(k));
                        //System.out.println("error za "+k+" "+j+" "+matrix2.get(k, j));
                    }
                }
                //System.out.println("RED "+i+" KOLONA "+j+" : "+sum);

                newMatrix.set(i, j, sum);
            }
        }
    }
}

