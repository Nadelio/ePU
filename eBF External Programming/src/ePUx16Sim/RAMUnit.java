package ePUx16Sim;

import eBF.DoubleByte;

public class RAMUnit {
    public static DoubleByte[][] RAM = new DoubleByte[256][256];

    public static void initalizeRAM(){
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                RAM[x][y] = new DoubleByte((byte) 0, (byte) 0);
            }
        }
    }

    public static void writeData(byte x, byte y, DoubleByte data) {
        int xAddr = x & 0xFF;
        int yAddr = y & 0xFF;
        RAM[xAddr][yAddr] = data;
    }

    public static DoubleByte readData(byte x, byte y) {
        int xAddr = x & 0xFF;
        int yAddr = y & 0xFF;
        return RAM[xAddr][yAddr];
    }
}
