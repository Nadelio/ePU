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

    public static UnsignedByte[] toUnsignedByte(String[] s){
        byte[] b = new byte[s.length];
        for(int i = 0; i < s.length; i++){
            b[i] = (byte) Integer.parseInt(s[i], 2);
        }

        UnsignedByte[] ub = new UnsignedByte[b.length];
        for(int i = 0; i < b.length; i++){
            ub[i] = new UnsignedByte(b[i]);
        }
        return ub;
    }
}
