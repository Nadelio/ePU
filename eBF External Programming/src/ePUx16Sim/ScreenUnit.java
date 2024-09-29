package ePUx16Sim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import eBF.Word;

import java.awt.image.BufferedImage;

public class ScreenUnit {

    private static JFrame frame;
    private static JPanel panel;
    private static BufferedImage screenImage;

    private static final int ramXMapAddress = 0;
    private static final int ramYMapAddress = 1;

    public static void intializeScreen(){ // setup screen
        frame = new JFrame("ePUx16");
        screenImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(screenImage, 0, 0, null);
            }
        };
        panel.setPreferredSize(new Dimension(256, 256));
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void writePixel(UnsignedByte x, UnsignedByte y, Word color) { // set a specific pixel to a specific color at location (x, y)
        if(color == null){ color = Word.zero(); }
        Color c = ScreenUnit.getColor(color);
        screenImage.setRGB(x.value, y.value, c.getRGB());
        panel.repaint();
    }

    public static void clearScreen() { // set every pixel to black
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                screenImage.setRGB(x, y, Color.BLACK.getRGB());
            }
        }
        panel.repaint();
    }

    /**
     * 
     * @param x : x address of first piece area of pixels in screen
     * @param y : y address of first piece area of pixels in screen
     * @param size : size of data dump
     */
    public static void pixelDataDump(UnsignedByte x, UnsignedByte y, Word size) { // reads in pixel data from RAM at (x, y) until size is met
        if(size.convertToInt() == 0){ return; }
        if(size.convertToInt() > 256){ return; }
        int i;
        for (i = 0; i < size.convertToInt(); i++) {
            Word pixelData = RAMUnit.readData(new UnsignedByte(ramXMapAddress + i), new UnsignedByte(ramYMapAddress));
            if(pixelData == null){ pixelData = Word.zero(); }
            ScreenUnit.writePixel(x, y, pixelData);
            x.value++;
            if (x.value == 256) {
                y.value++;
            }
        }
    }

    private static Color getColor(Word color) {
        int value = color.convertToInt();
        int red = (value & 0xFF0000) >> 16;
        int green = (value & 0x00FF00) >> 8;
        int blue = value & 0x0000FF;

        return new Color(red, green, blue);
    }

    // Screen is 256 x 256 pixels
    // Screen has 2^16 colors
}