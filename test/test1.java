import java.util.ArrayList;
import java.util.Arrays;

public class test1 {


    public static void main(String[] args) {
        String[] array1 = new String[4];
        int[] array2 = new int[4];
    
        ArrayList<String> arraylist1 = new ArrayList<>(Arrays.asList("test1","test2"));

        
        System.out.println("test1");
        System.out.println(arraylist1.get(1));

        arraylist1.add("prova1");
        System.out.println(arraylist1.size());

    }



}
