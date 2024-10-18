package task.workers;

import matrix.M_Matrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class FileExtractorWorker implements Callable<List<M_Matrix>> {


    private File file;//fajl iz kojeg izvlacimo matricu
    private final List<File>filesToExtract;




    public FileExtractorWorker(List<File> filesToExtract){
        this.filesToExtract=filesToExtract;
    }


    @Override
    public List<M_Matrix> call() throws Exception {
        List<M_Matrix>results=new ArrayList<>();
        String line;
        boolean firstLine=false;
        for (File file: this.filesToExtract) {
        //System.out.println(Thread.currentThread().getName()+" Skenira fajl: "+file.getName());
            M_Matrix newMatrix;
            BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));

            //Izvlacimo prvu liniju
            line=reader.readLine();
            String[] tokens = line.split("\\s+");

            //Testiraj ovo
            String[] parts = line.split(",\\s*|\\s*=\\s*"); // Splitting by comma or equal sign with optional spaces
            String matrixName = parts[1];
            int rows = Integer.parseInt(parts[3]);
            int cols = Integer.parseInt(parts[5]);


            newMatrix=new M_Matrix(matrixName,rows,cols,file);

            /**
             * matrix_name=A1, rows=146, cols=97
             * 1,0 = 363
             * 95,0 = 7750210
             * 100,5 = 5447436
             *
             */

            int lastRow=0;
            int lastCol=0;
            try {
                while ((line = reader.readLine()) != null) {
                    //ne zaboravi da popunis nulama
                    tokens = line.split("\\s+");
                    String[] ij=tokens[0].split(",");
                    int i=Integer.parseInt(ij[0]);
                    int j=Integer.parseInt(ij[1]);
                    BigInteger value=BigInteger.valueOf(Long.parseLong(tokens[2]));
                    //SADA TREBA DA STAVIS POISONE I ZA OSTALE QUEUE-OVE
                    //SADA TREBA DA STAVIS POISONE I ZA OSTALE QUEUE-OVE
                    //SADA TREBA DA STAVIS POISONE I ZA OSTALE QUEUE-OVE
                    //TESTIRATI PRETRAGU MATRICA
                    //TESTIRATI PRETRAGU MATRICA
                    //TESTIRATI PRETRAGU MATRICA
                    //TESTIRATI PRETRAGU MATRICA
                    //TESTIRATI PRETRAGU MATRICA

                    newMatrix.set(i,j,value);
                    newMatrix.fillWithZeros(lastRow,lastCol,i,j);
                    lastRow=i;
                    lastCol=j;
                }
                reader.close();
            }
            catch (Exception e) {
               e.printStackTrace();
            }
            //System.out.println("Kreirana matrica "+newMatrix.getName());
//            MainCLI.jobQueue.put(new TaskSquareMatrix(newMatrix));

            //ukoliko poslednji red fajla ne zavrsava sa poslednjim redom matrice(kolone),treba da popunimo nulama
            if(lastRow!=newMatrix.getRows()-1 || lastCol!=newMatrix.getCols()-1){
                newMatrix.fillWithZeros(lastRow,lastCol,newMatrix.getRows()-1,newMatrix.getCols()-1);
            }
            results.add(newMatrix);

        }


        return results;
    }




}


