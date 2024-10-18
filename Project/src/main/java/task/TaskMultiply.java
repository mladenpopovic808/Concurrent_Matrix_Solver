package task;

import matrix.M_Matrix;

//Mnozenje dve prethodno definisane matrice
public class TaskMultiply implements Task{

    private final boolean poison;
    private final M_Matrix matrix1;
    private final M_Matrix matrix2;
    private final String name;


    public TaskMultiply(boolean poison) {
        this.poison = poison;
        matrix1=null;
        matrix2=null;
        name ="";
    }


    //TODO ovde verovatno ce moci da se prosledi i ime nove matrice,a ako se ne prosledi radi se konkatenacija
    public TaskMultiply(M_Matrix matrix1,M_Matrix matrix2,String name){
        this.matrix1=matrix1;
        this.matrix2=matrix2;
        if(name==null) {
            this.name = matrix1.getName() + matrix2.getName();
        }else{
            this.name =name;
        }

        poison=false;
    }

    @Override
    public TaskType getType() {
        return TaskType.MULTIPLY;
    }
    @Override
    public boolean isPoison() {
        return poison;
    }

    public M_Matrix getMatrix1() {
        return matrix1;
    }

    public M_Matrix getMatrix2() {
        return matrix2;
    }

    public String getName() {
        return name;
    }
}
