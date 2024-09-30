package ePUx16Sim;

public class BIOS {
    // this will be the entry point of the ePUx16 sim
    // this will start the computer and load the OS (if it exists)
    // OS will be located in the X:00000000 Y:00000000 location of the ROM

    // start computer -> call PC to load OS -> PC runs OS -> OS calls PC to load/run programs (multiple threads?)

    public static void main(String [] args) throws Exception{
        UnsignedByte zero = UnsignedByte.zero();
        UnsignedByte one = new UnsignedByte(1);
        UnsignedByte two = new UnsignedByte(2);
        UnsignedByte nine = new UnsignedByte(9);
        ControlUnit.commandUnit(new UnsignedByte[]{ one }); // start computer
        System.out.println("| Loading OS |");
        ControlUnit.commandUnit(new UnsignedByte[]{ two, two, zero, zero, nine }); // load and start OS ( 2 : { x, y } { size } )
    }
}