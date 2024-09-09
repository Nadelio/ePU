public class DoubleByte {
    private byte highByte;
    private byte lowByte;
    private static int size = 16;

    public DoubleByte(byte highByte, byte lowByte){
        this.highByte = highByte;
        this.lowByte = lowByte;
    }

    public byte[] getValue(){ return new byte[]{ highByte, lowByte }; }
    public byte getHighByte(){ return highByte; }
    public byte getLowByte(){ return lowByte; }
    public int getSize(){ return size; }

    public DoubleByte AND(DoubleByte secondByte){
        byte newHighByte = (byte) (this.highByte & secondByte.getHighByte());
        byte newLowByte = (byte) (this.lowByte & secondByte.getLowByte());
        return new DoubleByte(newHighByte, newLowByte);
    }

    public DoubleByte OR(DoubleByte secondByte){
        byte newHighByte = (byte) (this.highByte | secondByte.getHighByte());
        byte newLowByte = (byte) (this.lowByte | secondByte.getLowByte());
        return new DoubleByte(newHighByte, newLowByte);
    }

    public DoubleByte NOT(){
        byte newHighByte = (byte) ~this.highByte;
        byte newLowByte = (byte) ~this.lowByte;
        return new DoubleByte(newHighByte, newLowByte);
    }
    
    public DoubleByte XOR(DoubleByte secondByte){
        byte newHighByte = (byte) (this.highByte ^ secondByte.getHighByte());
        byte newLowByte = (byte) (this.lowByte ^ secondByte.getLowByte());
        return new DoubleByte(newHighByte, newLowByte);
    }
    
    public DoubleByte NAND(DoubleByte secondByte){
        return this.AND(secondByte).NOT();
    }

    public DoubleByte NOR(DoubleByte secondByte){
        return this.OR(secondByte).NOT();
    }

    public DoubleByte XNOR(DoubleByte secondByte){
        return this.XOR(secondByte).NOT();
    }
}
