package ePUx16Sim;

import eBF.DoubleByte;

public class ProgramCounterUnit {

    public static void requestStartProgram(byte x, byte y, DoubleByte size) throws Exception {
        Registers.readInProgram(x, y, size);
        Registers.startProgram(x, y, size);
    }

    // request data from ROM via the registers, then load it into the Program Counter
    // use embedded version of eBF compiler and interpreter
    //! Don't forget to swap the '=' symbol with the '$' symbol in the embedded eBF compiler

    //! primarily used for constants that are saved, like passwords, not for use with reading programs into PC Unit
    public static DoubleByte readData(byte x, byte y) { return ROMUnit.readData(x, y); }
}