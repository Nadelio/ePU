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
        eBF.EmbeddedeBFInterpreter.initializeStack();
        System.out.println("| Program Running |");
        eBF.EmbeddedeBFInterpreter.interpretEBIN(programFile);
    }

    //! Don't forget to compile to .jar // unable to compile/run on laptop, need to compile/run on desktop
    public static int findSize(UnsignedByte x, UnsignedByte y) { //! ISSUE WITH NOT DETECTING END OF PROGRAM
        System.out.println("| Finding Program Size |");
        int size = 0;
        while(!((ROMUnit.readData(x, y).convertToInt()) == (new Word(UnsignedByte.zero(), new UnsignedByte(15)).convertToInt()))){ //! HERE
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
            if((ROMUnit.readData(x, y).convertToInt()) == (new Word(UnsignedByte.zero(), new UnsignedByte(15)).convertToInt())){ break; } //! HERE
        }
        System.out.println("| Found Program Size |");
        System.out.println("| Program Size: " + size + " |");
        return size;
    }

    // Registers are the same size as ROM and RAM
    // Registers are program data only
    // Registers read in data from ROM at the request of the Program Counter
    // Registers need to dynamically load programs from ROM based on Dependencies set inside the mother program
}
