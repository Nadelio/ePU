package ePUx16Sim;

public class BIOS {
    // this will be the entry point of the ePUx16 sim
    // this will start the computer and load the OS (if it exists)
    // OS will be located in the X:00000000 Y:00000000 location of the ROM

    // start computer -> call PC to load OS -> PC runs OS -> OS calls PC to load/run programs (multiple threads?)

    public static void main(String [] args){
        ControlUnit.commandUnit(new byte[]{0x01}); // start computer
        ControlUnit.commandUnit(new byte[]{0x02, 0x00, 0x00}); // load and start OS
    }
}
