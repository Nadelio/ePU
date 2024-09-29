package ePUx16Sim;

public class UnsignedByte {
    private byte rawValue;
    public int value;

    public UnsignedByte(byte rawValue) {
        this.rawValue = rawValue;
        this.value = rawValue & 0xFF;
    }

    public UnsignedByte(int value) {
        this.value = value;
        this.rawValue = (byte) value;
    }

    public byte getRawValue() { return rawValue; }
    public static UnsignedByte zero() { return new UnsignedByte(0); }
}
