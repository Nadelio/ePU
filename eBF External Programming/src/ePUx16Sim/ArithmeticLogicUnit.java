package ePUx16Sim;

import eBF.Word;

public class ArithmeticLogicUnit {

    private static byte out;

    public static void evaluate(UnsignedByte op, UnsignedByte a, UnsignedByte b) {
        byte opCode = op.getRawValue();
        opCode -= (byte) 0x70;
        switch(opCode){
            case 0x00: // add
                out = (byte) (a.value + b.value);
                break;
            case 0x01: // sub
                out = (byte) (a.value - b.value);
                break;
            case 0x02: // mul
                out = (byte) (a.value * b.value);
                break;
            case 0x03: // div
                out = (byte) (a.value / b.value);
                break;
            case 0x04: // mod
                out = (byte) (a.value % b.value);
                break;
            case 0x05: // and
                out = (byte) (a.value & b.value);
                break;
            case 0x06: // or
                out = (byte) (a.value | b.value);
                break;
            case 0x07: // xor
                out = (byte) (a.value ^ b.value);
                break;
            case 0x08: // not
                out = (byte) ~a.value;
                break;
            default:
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
