package ePUx16Sim;

import eBF.Word;

public class ArithmeticLogicUnit {

    private static byte out;

    public static void evaluate(UnsignedByte op, UnsignedByte a, UnsignedByte b) {
        switch(op.value){
            case 0: // add
                out = (byte) (a.value + b.value);
                break;
            case 1: // sub
                out = (byte) (a.value - b.value);
                break;
            case 2: // mul
                out = (byte) (a.value * b.value);
                break;
            case 3: // div
                out = (byte) (a.value / b.value);
                break;
            case 4: // mod
                out = (byte) (a.value % b.value);
                break;
            case 5: // and
                out = (byte) (a.value & b.value);
                break;
            case 6: // or
                out = (byte) (a.value | b.value);
                break;
            case 7: // xor
                out = (byte) (a.value ^ b.value);
                break;
            case 8: // not
                out = (byte) ~a.value;
                break;
            default: // flush
                out = 0x00;
                break;
        }
    }

    // just build basic 8 bit ALU (add, sub, mul, div, mod, and, or, xor, not)
    // ALU sys call is 4 bits long
    // op input is 4 bits long
    // A input is 8 bits long
    // B input is 8 bits long
    // total of 24 bits or 3 bytes:
    // 0111OOOO AAAAAAAA BBBBBBBB

    // Any op code outside of 0x70 - 0x78 will return 0x00 (aka flush the ALU)

    public static Word readData() {
        return new Word(new UnsignedByte(0), new UnsignedByte(out));
    }
}
