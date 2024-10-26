package ePUx16Sim;

import java.io.File;
import java.io.FileWriter;

public class Registers {

    private static Word[][] register = new Word[256][256];

    public static void initializeRegisters() {
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                register[i][j] = Word.zero();
            }
        }
    }
    
    public static void readInProgram(UnsignedByte x, UnsignedByte y, Word size) {
        if(size.isZero()){ System.out.println("| Program Size is Zero, Returning |"); return; }
        int xPos = x.value;
        int yPos = y.value;
        for(int i = 0; i < size.convertToInt(); i++){
            register[xPos][yPos] = ROMUnit.readData(new UnsignedByte(xPos), new UnsignedByte(yPos));
            yPos++;
            if(yPos == 256){
                yPos = 0;
                xPos++;
            }
        }
    }

    public static void startProgram(UnsignedByte x, UnsignedByte y, Word size) throws Exception {
        if(size.isZero()){ System.out.println("| Program Size is Zero, Returning |"); return; }
        
        File programFile = new File("program.ebin");
        FileWriter fw = new FileWriter(programFile);

        int xPos = x.value;
        int yPos = y.value;
        for(int i = 0; i < size.convertToInt(); i++){
            fw.write(register[xPos][yPos].toString() + " ");
            yPos++;
            if(yPos == 256){
                yPos = 0;
                xPos++;
            }
        }

        fw.close();

        System.out.println("| Program Size: " + size.convertToInt() + " |");
        System.out.println("| Program File Size: " + programFile.length() + " |");

        System.out.println("| Initializing Stack |");
        ePUx16Sim.EmbeddedeBFInterpreter.initializeStack();
        System.out.println("| Program Running |");
        ePUx16Sim.EmbeddedeBFInterpreter.interpretEBIN(programFile);
    }

    public static int findSize(UnsignedByte x, UnsignedByte y) throws Exception {
        final String END_TOKEN = "0000000000001111";
        System.out.println("| Finding Program Size |");
        Word exactPosition = new Word(x, y);
        String rawData = ROMUnit.requestRawRomData();
        String programData = rawData.substring(exactPosition.convertToInt() * 17, rawData.indexOf(END_TOKEN, exactPosition.convertToInt()) + 17);
        int size = (programData.length())/17;
        System.out.println("| Found Program Size |");
        System.out.println("| Program Size: " + size + " |");
        return size;
    }

    // Registers are the same size as ROM and RAM
    // Registers are program data only
    // Registers read in data from ROM at the request of the Program Counter
    // Registers need to dynamically load programs from ROM based on Dependencies set inside the mother program
}
