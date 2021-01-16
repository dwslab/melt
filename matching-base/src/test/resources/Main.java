import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Random;


public class Main {
    public static void main(String[] args){
        int linesToProduce = 100;
        if(args.length >= 1){
            try{
                linesToProduce = Integer.parseInt(args[0]);
            }catch(NumberFormatException ex){}
        }
        Random rnd = new Random(1324);
        for(int i=0; i < linesToProduce; i++){
            byte[] array = new byte[200];
            rnd.nextBytes(array);
            System.out.println(new String(array, StandardCharsets.UTF_8));
        }
        File f = new File("Main.java");
        System.out.println(f.toURI().toString());
    }
}
