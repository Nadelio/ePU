package Tooling;

import java.io.FileWriter;
import java.io.File;

import java.nio.file.Files;
import java.nio.file.Paths;

import eBF.Word;
import ePUx16Sim.UnsignedByte;

public class WriteToROM {

    public static Word[][] ROM = new Word[256][256];

    public static void main(String[] args) {

        
        // write ROM data to file
        FileWriter fw;
        try {
            File raw = new File(args[0]);
            String romData = new String(Files.readAllBytes(Paths.get(raw.getCanonicalPath())));
            String[] romDataArray = romData.split(" ");

            int x = 0;
            int y = 0;
            for(String data : romDataArray){
                ROM[y][x] = convertToWord(data);
                x++;
                if(x == 256){
                    x = 0;
                    y++;
                }
            }


            fw = new FileWriter(args[1]);

            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    if (ROM[i][j] != null) {
                        fw.write(ROM[i][j].toString() + " ");
                    }
                }
            }

            fw.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static Word convertToWord(String data) {
        UnsignedByte[] bytes = UnsignedByte.toUnsignedByte(new String[]{ data.substring(0, 8), data.substring(8) });
        return new Word(bytes[0], bytes[1]);
    }
}