package eBF;

import ePUx16Sim.UnsignedByte;

public class Word {
    private UnsignedByte highByte;
    private UnsignedByte lowByte;
    private static int size = 16;

    
    public Word(UnsignedByte highByte, UnsignedByte lowByte){
        this.highByte = highByte;
        this.lowByte = lowByte;
    }

    public UnsignedByte[] getValue(){ return new UnsignedByte[]{ highByte, lowByte }; }
    public UnsignedByte getHighByte(){ return highByte; }
    public UnsignedByte getLowByte(){ return lowByte; }
    public int getSize(){ return size; }
    public static Word zero(){ return new Word(UnsignedByte.zero(), UnsignedByte.zero()); }

    public boolean isZero(){ return (highByte.getRawValue() == 0) && (lowByte.getRawValue() == 0); }

    public void setHighByte(UnsignedByte highByte){ this.highByte = highByte; }
    public void setLowByte(UnsignedByte lowByte){ this.lowByte = lowByte; }

    public static Word convertToWord(int value){
        UnsignedByte highByte = new UnsignedByte(value >> 8);
        UnsignedByte lowByte = new UnsignedByte(value);
        return new Word(highByte, lowByte);
    }

    public int convertToInt(){
        return (highByte.value << 8) | lowByte.value;
    }

    public Word AND(Word secondByte) {
        UnsignedByte newHighByte = new UnsignedByte(this.highByte.value & secondByte.getHighByte().value);
        UnsignedByte newLowByte = new UnsignedByte(this.lowByte.value & secondByte.getLowByte().value);
        return new Word(newHighByte, newLowByte);
    }
    
    public Word OR(Word secondByte) {
        UnsignedByte newHighByte = new UnsignedByte(this.highByte.value | secondByte.getHighByte().value);
        UnsignedByte newLowByte = new UnsignedByte(this.lowByte.value | secondByte.getLowByte().value);
        return new Word(newHighByte, newLowByte);
    }
    
    public Word NOT() {
        UnsignedByte newHighByte = new UnsignedByte(~this.highByte.value);
        UnsignedByte newLowByte = new UnsignedByte(~this.lowByte.value);
        return new Word(newHighByte, newLowByte);
    }
    
    public Word XOR(Word secondByte) {
        UnsignedByte newHighByte = new UnsignedByte(this.highByte.value ^ secondByte.getHighByte().value);
        UnsignedByte newLowByte = new UnsignedByte(this.lowByte.value ^ secondByte.getLowByte().value);
        return new Word(newHighByte, newLowByte);
    }
    
    public Word NAND(Word secondByte) {
        return this.AND(secondByte).NOT();
    }
    
    public Word NOR(Word secondByte) {
        return this.OR(secondByte).NOT();
    }
    
    public Word XNOR(Word secondByte) {
        return this.XOR(secondByte).NOT();
    }
    
    public String toString() {
        return Integer.toBinaryString(highByte.value) + "" + Integer.toBinaryString(lowByte.value);
    }
}
