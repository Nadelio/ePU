package ePUx16Sim;

public class ProgramCounterUnit {

    public static void requestStartProgram(UnsignedByte x, UnsignedByte y, Word size) throws Exception {
        System.out.println("| Loading Program |");
        Registers.readInProgram(x, y, size);
        System.out.println("| Starting Program |");
        Registers.startProgram(x, y, size);
    }

    // request data from ROM via the registers, then load it into the Program Counter
    // use embedded version of eBF compiler and interpreter
    //! Don't forget to swap the '=' symbol with the '$' symbol in the embedded eBF compiler

    //! primarily used for constants that are saved, like passwords, not for use with reading programs into PC Unit
    public static Word readData(UnsignedByte x, UnsignedByte y) { return ROMUnit.readData(x, y); }
}