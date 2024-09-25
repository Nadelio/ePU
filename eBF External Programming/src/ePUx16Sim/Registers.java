package ePUx16Sim;

import eBF.DoubleByte;
import java.io.File;
import java.io.FileWriter;

public class Registers {

    private static DoubleByte[][] register = new DoubleByte[256][256];
    
    public static void readInProgram(byte x, byte y, DoubleByte size) {
        for (int i = 0; i < size.convertToInt(); i++) {
            register[x][y] = ROMUnit.readData(x, y);
            x++;
            if(x <= 256){
                x = 0;
                y++;
            }
        }
    }

    public static void startProgram(byte x, byte y, DoubleByte size) throws Exception {
        File programFile = new File("program.ebin");
        FileWriter fw = new FileWriter(programFile);

        for(int i = 0; i < size.convertToInt(); i++){
            fw.write(register[x][y].toString());
            x++;
            if(x <= 256){
                x = 0;
                y++;
            }
        }

        fw.close();

        eBF.eBFInterpreter.initializeStack();
        eBF.eBFInterpreter.interpretEBIN(programFile);
    }

    // Registers are the same size as ROM and RAM
    // Registers are program data only
    // Registers read in data from ROM at the request of the Program Counter
    // Registers need to dynamically load programs from ROM based on Dependencies set inside the mother program
}
