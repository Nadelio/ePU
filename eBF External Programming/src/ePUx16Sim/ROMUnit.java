package ePUx16Sim;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import eBF.Word;

public class ROMUnit {

    private static final int ramXMapAddress = 0;
    private static final int ramYMapAddress = 0;

    private static Word[][] ROM = new Word[256][256];
    private static boolean[][] protectedMemory = new boolean[256][256];

    public static void requestWriteData(UnsignedByte x, UnsignedByte y, Word data) {
        if(x.value < 0 || x.value > 255 || y.value < 0 || y.value > 255){ return; }
        if(protectedMemory[x.value][y.value]){ return; }
        ROM[x.value][y.value] = data;
    }

    public static void requestWriteDataHeap(UnsignedByte x, UnsignedByte y, Word size) {
        for(int i = 0; i < size.convertToInt(); i++){
            if(x.value < 0 || x.value > 255 || y.value < 0 || y.value > 255){ return; }
            if(protectedMemory[x.value][y.value]){ return; }
            ROM[x.value][y.value] = RAMUnit.readData(new UnsignedByte(ramXMapAddress + i), new UnsignedByte(ramYMapAddress + i));
            x.value++;
            if(x.value == 256){
                x.value = 0;
                y.value++;
            }
        }
    }

    public static void setProtectedMemory(UnsignedByte x, UnsignedByte y, Word size) { // protected data rule //! possible iteration issue, check ScreenUnit data dump method for possible solution
        for(int i = 0; i < size.convertToInt(); i++){
            protectedMemory[x.value][y.value] = true;
            x.value++;
            if(x.value == 256){
                x.value = 0;
                y.value++;
            }
        }
    } // PC Unit will call this and protect the current program data
    
    public static Word readData(UnsignedByte x, UnsignedByte y) { return ROM[x.value][y.value]; }

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
        // read ROM data from file
        // read protected memory data from file
        // read empty memory data from file
    }
}
