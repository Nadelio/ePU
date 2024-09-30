package ePUx16Sim;

import eBF.Word;
import java.io.File;
import java.io.FileWriter;

public class Registers {

    private static Word[][] register = new Word[256][256];
    
    public static void readInProgram(UnsignedByte x, UnsignedByte y, Word size) {
        if(size.isZero()){ System.out.println("| Program Size is Zero, Returning |"); return; }
        for (int i = 0; i < size.convertToInt(); i++) {
            register[x.value][y.value] = ROMUnit.readData(x, y);
            x.value++;
            if(x.value == 256){
                x.value = 0;
                y.value++;
            }
        }
    }

    public static void startProgram(UnsignedByte x, UnsignedByte y, Word size) throws Exception {
        if(size.isZero()){ System.out.println("| Program Size is Zero, Returning |"); return; }
        
        File programFile = new File("program.ebin");
        FileWriter fw = new FileWriter(programFile);

        for(int i = 0; i < size.convertToInt(); i++){
            fw.write(register[x.value][y.value].toString());
            x.value++;
            if(x.value == 256){
                x.value = 0;
                y.value++;
            }
        }

        fw.close();

        System.out.println("| Program Size: " + size.convertToInt() + " |");
        System.out.println("| Program File Size: " + programFile.length() + " |");

        System.out.println("| Initializing Stack |");
        eBF.eBFInterpreter.initializeStack();
        System.out.println("| Program Running |");
        eBF.eBFInterpreter.interpretEBIN(programFile);
    }

    public static int findSize(UnsignedByte x, UnsignedByte y) {
        System.out.println("| Finding Program Size |");
        int size = 0;
        while(!((ROMUnit.readData(x, y).convertToInt()) == (new Word(UnsignedByte.zero(), new UnsignedByte(15)).convertToInt()))){
            if(ROMUnit.readData(x, y).convertToInt() != 0) {
                System.out.println("ROM Data: " + ROMUnit.readData(x, y).convertToInt());
                System.out.println("Size: " + size);
                System.out.println("X: " + x.value + " Y: " + y.value + "\n");
            }
            size++;
            x.value++;
            if(x.value == 256){
                x.value = 0;
                y.value++;
                if(y.value == 256){
                    System.out.println("| Program Size Exceeds Maximum Size |");
                    return 0;
                }
            }
            if((ROMUnit.readData(x, y).convertToInt()) == (new Word(UnsignedByte.zero(), new UnsignedByte(15)).convertToInt())){ break; }
        }
        return size;
    }

    // Registers are the same size as ROM and RAM
    // Registers are program data only
    // Registers read in data from ROM at the request of the Program Counter
    // Registers need to dynamically load programs from ROM based on Dependencies set inside the mother program
}
