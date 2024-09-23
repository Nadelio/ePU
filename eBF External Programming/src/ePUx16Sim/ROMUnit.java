package ePUx16Sim;

public class ROMUnit {

    public static void requestWriteData(byte b, byte c, byte d) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'requestWriteData'");
    }
    
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
