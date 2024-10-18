package matrix;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class M_Matrix {

    //final, jer se nece menjati
    private final String name;
    private final int rows;
    private final int cols;
    private final Map<Integer, List<BigInteger>> values;
    private File file;

    private boolean isMultiplied; //da li je ova matrica rezultat mnozenja
    private M_Matrix matrix1; //prva matrica za mnozenje
    private M_Matrix matrix2; //druga matrica za mnozenje


    //‘A | rows = 1000, cols = 1000 | matrix_file.rix’
    @Override
    public String toString() {
        if(file==null)
            return name+" | rows="+rows+", cols="+cols;
        else{
            return name+" | rows="+rows+", cols="+cols+" | "+file.getName();
        }
    }



    //Ako je matrica proizvod
    public M_Matrix(String name,int rows,int cols,File file,boolean isMultiplied,M_Matrix matrix1,M_Matrix matrix2){
        this.name=name;
        this.rows=rows;
        this.cols=cols;
        this.file=file;
        this.isMultiplied=isMultiplied;
        this.matrix1=matrix1;
        this.matrix2=matrix2;
        this.values = new ConcurrentHashMap<>();
        for (int i = 0; i < rows; i++) {
            CopyOnWriteArrayList<BigInteger> rowList = new CopyOnWriteArrayList<>(new BigInteger[cols]);
            this.values.put(i, rowList);
        }
    }


    //Za prosledjivanje vrednosti ,napraviti klasu koja ima i,j,value
    public M_Matrix(String name, int rows, int cols, File file){
        this.values = new ConcurrentHashMap<>();//treba da joj dodas copyOnWriteArrayList()
        this.name=name;
        this.rows=rows;
        this.cols=cols;
        this.file=file;
        isMultiplied=false;

        for (int i = 0; i < rows; i++) {
            CopyOnWriteArrayList<BigInteger> rowList = new CopyOnWriteArrayList<>(new BigInteger[cols]);
            // Ovde smo kreirali niz BigInteger objekata odgovarajuće veličine i prosledili ga kao argument
            // konstruktoru CopyOnWriteArrayList, čime se unapred postavlja početna veličina liste.
            this.values.put(i, rowList);
        }
        //System.out.println("Napravio sam matricu "+values.get(0).size());

    }

    //TODO:Kada vise niti setuju vrednosti, npr prvo setujemo 1,0 pa 19,0 --> Treba da izmedju 1 i 19 setujes nule!

    //Vise niti ce da ulazi ovde i setuje, jer ce se podeliti poslovi.
    //Da li treba da bude synchronized? Ne znam, posto imamo concurentHashMap
    public void set(int i,int j,BigInteger value){

        //Ako ne postoji lista,napravi je.
        try{
        values.computeIfAbsent(i, k -> new CopyOnWriteArrayList<>()).set(j, value);
        //System.out.println("setujem "+i + " "+ j+" "+value.toString());

        }catch (Exception e){
            System.out.println("IndexOutOfBounds["+name+"] [rows]="+rows+" [cols]="+cols+" i="+i+" j="+j);
        }

    }
    public List<BigInteger> getRow(int i){
        return values.get(i);

    }
    public List<BigInteger> getColumn(int i){
        List<BigInteger> column=new ArrayList<>();
        for(int j=0;j<rows;j++){
            column.add(this.get(j,i));
        }
        return column;
    }

    public boolean isMultiplied() {
        return isMultiplied;
    }

    public M_Matrix getMatrix1() {
        return matrix1;
    }

    public M_Matrix getMatrix2() {
        return matrix2;
    }

    public void setMatrix1(M_Matrix matrix1) {
        this.matrix1 = matrix1;
    }

    public void setMatrix2(M_Matrix matrix2) {
        this.matrix2 = matrix2;
    }

    public void setMultiplied(boolean multiplied) {
        isMultiplied = multiplied;
    }

    public M_Matrix getTransponedMatrix(){
        M_Matrix transponedMatrix=new M_Matrix(name+"_transponed",cols,rows,file);
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                transponedMatrix.set(j,i,this.get(i,j));
            }
        }
        return transponedMatrix;
    }
    public BigInteger get(int i, int j) {
        List<BigInteger> innerList = values.getOrDefault(i, new CopyOnWriteArrayList<>());

        if (j >= 0 && j < innerList.size()) {
            return innerList.get(j);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     *
     110,0 = 11820564
     116,0 = 12089495
     117,0 = 12089495
     0,5 = 13170144
     1,1 = 12236131
     */

    //Popunjava nulama izmedju 2 polja
    public void fillWithZeros(int iLow, int jLow, int iHigh, int jHigh) {
        if(get(iLow,jLow)==null){
            set(iLow,jLow,BigInteger.ZERO);
        }if(get(iHigh,jHigh)==null) {
            set(iHigh, jHigh, BigInteger.ZERO);
        }
        //TODO obrisi if
        //TODO obrisi if
        //TODO obrisi if
        //TODO obrisi if





          // System.out.println("Evo me");

           if (jLow == jHigh) {
               for (int i = iLow + 1; i < iHigh; i++) {
                   if (i >= rows) {
                       System.out.println("radim breaak");
                       break;
                   }

                   //System.out.println("setujem za " + i + " " + jLow);
                   this.set(i, jLow, BigInteger.ZERO);
               }
           } else if (jLow < jHigh) {
               for (int j = jLow; j < jHigh; j++) {
                   for (int i = iLow; i < rows; i++) {
                      // System.out.println("setujem za " + i + " " + j);
                       this.set(i, j, BigInteger.ZERO);
                   }
               }
               for(int i=0;i<iHigh;i++){
                   //System.out.println("setujem za " + i + " " + jHigh);
                   this.set(i,jHigh,BigInteger.ZERO);
               }
           }






    }
    //matrix_name=C3, rows=99, cols=194
    public String getFileText(String fileName) {
        //matrix_name=A2, rows=117, cols=196

        String infoLine="matrix_name="+name+", rows="+rows+", cols="+cols;
        StringBuilder sb = new StringBuilder();
        sb.append(infoLine).append("\n");
        String tmpLine;

        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                BigInteger value = this.get(i, j);
                if (value != null) {
                    tmpLine=i+","+j+"="+value+"\n";
                    infoLine+=tmpLine;
                    sb.append(i).append(",").append(j).append(" = ").append(value).append("\n");

                }
            }
        }

        return sb.toString();
    }


    public File getFile() {
        return file;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public Map<Integer, List<BigInteger>> getValues() {
        return values;
    }

    public String getName() {
        return name;
    }

    public void setFile(File file) {
        this.file = file;
    }
}

















