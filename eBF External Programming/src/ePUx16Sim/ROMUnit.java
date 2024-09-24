package ePUx16Sim;

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

    public static void saveToFile() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveToFile'");
    }

    public static void loadFromFile() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadFromFile'");
    }

    // ROM written to a file that is then read into ROM on startup via BIOS
    // before stopping computer, write all ROM data to file
}
