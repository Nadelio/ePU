package Tooling;

import ePUx16Sim.Word;
import ePUx16Sim.UnsignedByte;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;

//! Still not fully fixed, issue with writing to proper memory locations, maybe incrementing the wrong cordinate?

public class WriteToROM {

    private static Word[][] ROM = new Word[256][256];
    private static String romData;

    public static void requestWriteData(UnsignedByte x, UnsignedByte y, Word data) {
        if(x.value < 0 || x.value > 255 || y.value < 0 || y.value > 255){ return; }
        ROM[x.value][y.value] = data;
    }

    public static void main(String[] args) throws IOException {
        loadFromFile();
        System.out.println("| Writing Data to ROM |");
        UnsignedByte[] writeCords = toUnsignedByte(new String[]{args[1], args[2]});
        File programData = new File(args[0]);
        String data = new String(Files.readAllBytes(Paths.get(programData.getCanonicalPath())));
        String[] dataArray = data.split(" ");
        Word[] pureData = toWordArray(dataArray);
        System.out.println("| Data Size: " + pureData.length + " |");
        int xOffset = 0;
        int yOffset = 0;
        for(Word word : pureData){
            System.out.println("| Writing to: " + writeCords[0].value + ", " + writeCords[1].value + " |");
            writeCords[0] = new UnsignedByte(writeCords[0].value + xOffset);
            writeCords[1] = new UnsignedByte(writeCords[1].value + yOffset);
            requestWriteData(writeCords[0], writeCords[1], word);
            xOffset++;
            if(xOffset == 256){
                xOffset = 0;
                yOffset++;
            }
            System.out.println("| Successfully Wrote to " + writeCords[0].value + ", " + writeCords[1].value + " |");
        }
        System.out.println("| Data Written to ROM |");
        saveToFile();
    }

    public static void saveToFile() throws IOException {
        System.out.println("| Saving ROM Data |");
        File memoryFile = new File("ePUx16SimData/raw.rom");
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
        System.out.println("| ROM Data Saved |");
    }

    public static void loadFromFile() throws IOException {
        System.out.println("| Loading ROM Data |");

        File raw = new File("ePUx16SimData/raw.rom");

        romData = new String(Files.readAllBytes(Paths.get(raw.getCanonicalPath())));

        String[] romDataArray = romData.split(" ");

        int x = 0;
        int y = 0;
        for (String data : romDataArray) {
            ROM[x][y] = convertToWord(data);
            x++;
            if (x == 256) {
                x = 0;
                y++;
            }
        }

        System.out.println("| ROM Data Loaded |");
    }

    public static UnsignedByte[] toUnsignedByte(String[] s){
        byte[] b = new byte[s.length];
        for(int i = 0; i < s.length; i++){
            if (!s[i].isEmpty()) {
                try{
                    b[i] = (byte) Integer.parseInt(s[i], 2);
                } catch (NumberFormatException e) {
                    b[i] = (byte) -1;
                }
            } else {
                throw new NumberFormatException("Empty string at index " + i);
            }
        }

        UnsignedByte[] ub = new UnsignedByte[b.length];
        for(int i = 0; i < b.length; i++){
            if(b[i] == -1){
                ub[i] = new UnsignedByte(255);
            } else {
                ub[i] = new UnsignedByte(b[i]);
            }
        }
        return ub;
    }

    public static Word convertToWord(String data){
        UnsignedByte[] bytes = toUnsignedByte(new String[]{ data.substring(0, 8), data.substring(8) });
        return new Word(bytes[0], bytes[1]);
    }

    public static Word[] toWordArray(String[] data){
        Word[] wordArray = new Word[data.length];
        for(int i = 0; i < data.length; i++){
            wordArray[i] = convertToWord(data[i]);
        }
        return wordArray;
    }

    public static String requestRawRomData(){ return romData; }
}