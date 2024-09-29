package ePUx16Sim;

import eBF.Word;
import java.io.File;
import java.io.FileWriter;

public class Registers {

    private static Word[][] register = new Word[256][256];
    
    public static void readInProgram(UnsignedByte x, UnsignedByte y, Word size) {
        if(size.isZero()){ return; }
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
        if(size.isZero()){ return; }
        
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

        eBF.eBFInterpreter.initializeStack();
        eBF.eBFInterpreter.interpretEBIN(programFile);
    }

    public static int findSize(UnsignedByte x, UnsignedByte y) {
        int size = 0;
        while(ROMUnit.readData(x, y).equals(new Word(UnsignedByte.zero(), new UnsignedByte(15)))){
            size++;
            x.value++;
            if(x.value == 256){
                x.value = 0;
                y.value++;
            }
        }
        return size;
    }

    // Registers are the same size as ROM and RAM
    // Registers are program data only
    // Registers read in data from ROM at the request of the Program Counter
    // Registers need to dynamically load programs from ROM based on Dependencies set inside the mother program
}
