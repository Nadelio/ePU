package ePUx16Sim;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import eBF.DoubleByte;

public class ROMUnit {

    private static DoubleByte[][] ROM = new DoubleByte[256][256];
    private static boolean[][] protectedMemory = new boolean[256][256];

    public static void requestWriteData(byte x, byte y, DoubleByte data) { // check if x and y are within bounds and if data is overwriting protected memory
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'requestWriteData'");
    }

    public static void requestWriteDataHeap(byte x, byte y, DoubleByte size) { // implement overflow protection rule here
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'requestWriteDataHeap'");
    }

    public static void setProtectedMemory(byte x, byte y, DoubleByte size) { // protected data rule
        for(int i = 0; i < size.convertToInt(); i++){
            protectedMemory[x & 0xFF][y & 0xFF] = true;
            x++;
            if(x == 256){
                x = 0;
                y++;
            }
        }
    } // PC Unit will call this and protect the current program data
    
    /*
    1. Can't override protected data (define protected data via sys call)
        - OS data
        - Current program data
    2. Can't write more data than you have available to ROM
    3. Can't directly read from ROM
        - Have to request for ROM data to be loaded into the Registers before you can then either
        - load it into RAM
        - have the Program Counter read it
    */

    public static void saveToFile() throws IOException {
        File memoryFile = new File("ROMraw.dat");
        File protectedMemoryFile = new File("ROMprotected.dat");
        File emptyMemoryFile = new File("ROMempty.dat");

        // write ROM data to file
        FileWriter fw = new FileWriter(memoryFile);

        for(int i = 0; i < 256; i++){
            for(int j = 0; j < 256; j++){
                if(ROM[i][j] != null){
                    fw.write(ROM[i][j] + " ");
                }
            }
        }

        fw.close();

        // write protected memory data to file
        fw = new FileWriter(protectedMemoryFile);

        for(int i = 0; i < 256; i++){
            for(int j = 0; j < 256; j++){
                if(protectedMemory[i][j]){
                    fw.write("true ");
                } else {
                    fw.write("false ");
                }
            }
        }

        fw.close();

        // write empty memory data to file
        fw = new FileWriter(emptyMemoryFile);

        for(int i = 0; i < 256; i++){
            for(int j = 0; j < 256; j++){
                if(ROM[i][j].isZero()){
                    fw.write("true ");
                } else {
                    fw.write("false");
                }
            }
        }

        fw.close();
    }

    public static void loadFromFile() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadFromFile'");
    }

    // ROM written to a file that is then read into ROM on startup via BIOS
    // before stopping computer, write all ROM data to file
}
