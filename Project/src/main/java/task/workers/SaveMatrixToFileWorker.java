package task.workers;

import matrix.M_Matrix;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveMatrixToFileWorker implements Runnable{


    private String matName;
    private String fileName;
    private M_Matrix matrix;


    public SaveMatrixToFileWorker(String matName, String fileName, M_Matrix matrix){
        this.matName=matName;
        this.fileName=fileName;
        this.matrix=matrix;

    }
    @Override
    public void run()  {
        String parentPath="src/main/resources/savedMatrices";
        File parentFile=new File(parentPath);
        if(!parentFile.exists()){
            parentFile.mkdirs();
        }
        File file;
        if(fileName.contains(".rix")) {
            file = new File(parentPath + "/" + fileName);
        }
        else{
            file=new File(parentPath+"/"+fileName+".rix");
            fileName+=".rix";
        }


        if(matrix.getFile()==null){//npr cuvamo izmnozenu matricu koja je nama u sistemu ali nema svoj fajl.
            matrix.setFile(file);
        }
        String fileText=matrix.getFileText(fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            //ako je u medjuvremenu obrisan
            if(!file.canWrite()) {
                System.err.println("Nije moguce upisati u fajl!");
                return;
            }
            writer.write(fileText);
            System.out.println("Uspesno upisano u fajl!");
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }


    }
}
