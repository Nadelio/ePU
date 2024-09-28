package ePUx16Sim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import eBF.DoubleByte;

import java.awt.image.BufferedImage;

public class ScreenUnit {

    private static JFrame frame;
    private static JPanel panel;
    private static BufferedImage screenImage;

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

    public static void writePixel(byte x, byte y, DoubleByte color) { // set a specific pixel to a specific color at location (x, y)
        Color c = ScreenUnit.getColor(color);
        screenImage.setRGB(x & 0xFF, y & 0xFF, c.getRGB());
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

    public static void pixelDataDump(byte x, byte y, DoubleByte size) { // reads in pixel data from RAM at (x, y) until size is met
        for (int i = 0; i < size.convertToInt(); i++) {
            DoubleByte pixelData = RAMUnit.readData(x, y);
            ScreenUnit.writePixel(x, y, pixelData);
            x++;
            if (x == 256) {
                x = 0;
                y++;
            }
        }
    }

    private static Color getColor(DoubleByte color) {
        int value = color.convertToInt();
        int red = (value & 0xFF0000) >> 16;
        int green = (value & 0x00FF00) >> 8;
        int blue = value & 0x0000FF;

        return new Color(red, green, blue);
    }

    // Screen is 256 x 256 pixels
    // Screen has 2^16 colors
}