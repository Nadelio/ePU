import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class eBFInterpreter {

    private static DoubleByte[] stack = new DoubleByte[256];
    private static int pointerX = 0;
    private static int pointerY = 0;
    private static int pointerValue = 0;
    private static DoubleByte[][] RAM = new DoubleByte[16][16];
    private static final int maxPointerValue = (int) Math.pow(2, 16) - 1;

    private static void interpretEBF(File eBFFile){
        
    }

    private static void interpretEBIN(File eBinaryFile) throws FileNotFoundException{
        Scanner sc = new Scanner(eBinaryFile);
        String eBinCode = "";
        while(sc.hasNextLine()){
            eBinCode += sc.nextLine() + " ";
        }
        sc.close();
        String[] eBinTokens = eBinCode.split(" ");
        // copy compiler switch case code here
    }

    private static void incrementPointerValue(){
        pointerValue++;
        if(pointerValue > maxPointerValue){
            pointerValue = 0;
        }
    }

    private static void decrementPointerValue(){
        pointerValue--;
        if(pointerValue < 0){
            pointerValue = maxPointerValue;
        }
    }

    private static void movePointerRight(){
        pointerX++;
        if(pointerX == 15){
            pointerX = 0;
            pointerY++;
            if(pointerY == 15){
                pointerY = 0;
                pointerX = 0;
            }
        }
    }

    private static void movePointerLeft(){
        pointerX--;
        if(pointerX == -1){
            pointerX = 15;
            pointerY--;
            if(pointerY == -1){
                pointerY = 15;
                pointerX = 15;
            }
        }
    }
}