package ePUx16Sim;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;

public class ROMUnit {

    private static final int ramXMapAddress = 0;
    private static final int ramYMapAddress = 0;

    private static Word[][] ROM = new Word[256][256];
    private static boolean[][] protectedMemory = new boolean[256][256];
    private static String romData;

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
    
    public static Word readData(UnsignedByte x, UnsignedByte y) { return ROM[y.value][x.value]; }

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
        File memoryFile = new File("ePUx16SimData/raw.rom");
        File protectedMemoryFile = new File("ePUx16SimData/protected.rom");
        File emptyMemoryFile = new File("ePUx16SimData/empty.rom");

        // write ROM data to file
        FileWriter fw = new FileWriter(memoryFile);

        for(int i = 0; i < 256; i++){
            for(int j = 0; j < 256; j++){
                if(ROM[i][j] != null){
                    fw.write(ROM[i][j].toString() + " ");
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
                    fw.write("false ");
                }
            }
        }

        fw.close();
    }

    public static void loadFromFile() throws IOException {
        // read ROM data from file
        // read protected memory data from file
        // read empty memory data from file

        System.out.println("| Loading ROM Data |");

        File raw = new File("ePUx16SimData/raw.rom");
        File protectedMem = new File("ePUx16SimData/protected.rom");

        romData = new String(Files.readAllBytes(Paths.get(raw.getCanonicalPath())));
        String protectedMemoryData = new String(Files.readAllBytes(Paths.get(protectedMem.getCanonicalPath())));

        String[] romDataArray = romData.split(" ");
        String[] protectedMemoryDataArray = protectedMemoryData.split(" ");

        System.out.println(romDataArray.length);
        System.out.println(protectedMemoryDataArray.length);

        // try {
            int x = 0;
            int y = 0;
            for(String data : romDataArray){
                ROM[x][y] = convertToWord(data);
                x++;
                if(x == 256){
                    x = 0;
                    y++;
                }
            }

            x = 0;
            y = 0;
            for(String data : protectedMemoryDataArray){
                protectedMemory[x][y] = Boolean.parseBoolean(data);
                x++;
                if(x == 256){
                    x = 0;
                    y++;
                }
            }
        // } catch (Exception e) {
            // System.out.println("| Error Loading ROM Data |");
            // System.out.println("| Resetting ROM Data |");
            // for(int i = 0; i < 256; i++){
            //     for(int j = 0; j < 256; j++){
            //         ROM[i][j] = Word.zero();
            //         protectedMemory[i][j] = false;
            //     }
            // }
            // System.out.println("| ROM Data Reset Complete |");
        // }
    }

    public static Word convertToWord(String data){
        UnsignedByte[] bytes = EmbeddedeBFInterpreter.toUnsignedByte(new String[]{ data.substring(0, 8), data.substring(8) });
        return new Word(bytes[0], bytes[1]);
    }

    public static String requestRawRomData(){ return romData; }
}
