package ePUx16Sim;

import eBF.DoubleByte;

public class ProgramCounterUnit {

    public static void requestStartProgram(byte b, byte c) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'requestStartProgram'");
    }

    // request data from ROM via the registers, then load it into the Program Counter
    // use embedded version of eBF compiler and interpreter
    //! Don't forget to swap the '=' symbol with the '$' symbol in the embedded eBF compiler and interpreter

    //! primarily used for constants that are saved, like passwords, not for use with reading programs into PC Unit
    public static DoubleByte readData() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readData'");
    }
}