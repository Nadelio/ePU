package ePUx16Sim;

import eBF.Word;

public class RAMUnit {
    public static Word[][] RAM = new Word[256][256];

    public static void initalizeRAM(){
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                RAM[x][y] = new Word(UnsignedByte.zero(), UnsignedByte.zero());
            }
        }
    }

    public static void writeData(UnsignedByte x, UnsignedByte y, Word data) {
        int xAddr = x.value;
        int yAddr = y.value;
        RAM[yAddr][xAddr] = data;
    }

    public static Word readData(UnsignedByte x, UnsignedByte y) {
        int xAddr = x.value;
        int yAddr = y.value;
        return RAM[yAddr][xAddr];
    }
}
