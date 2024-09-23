package ePUx16Sim;

public class ScreenUnit {

    public static void writePixel(byte x, byte y, byte color) { // set a specific pixel to a specific color at location (x, y)
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writePixel'");
    }

    public static void clearScreen() { // set every pixel to black
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clearScreen'");
    }

    public static void pixelDataDump(byte x, byte y, byte size) { // reads in pixel data from RAM at (x, y) until size is met
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pixelDataDump'");
    }

    // Screen is 256 x 256 pixels
    // Screen has 256 colors
    // write one pixel at a time (or add more sys calls to dump a bunch of pixel data to draw at once?)
    // clear screen sys call sets all pixels to color 0 (black)
}