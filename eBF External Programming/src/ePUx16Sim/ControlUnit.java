package ePUx16Sim;

import eBF.DoubleByte;

public class ControlUnit {
    private static boolean STARTED_FLAG = false;

    public static void commandUnit(byte[] command){
        switch(command[0]){
            case 0x00: // stop computer
                shutdownProtocol();
                System.exit(0);
                break;
            case 0x01: // start computer
                startupProtocol();
                break;
            case 0x02:
                if(STARTED_FLAG){
                    ProgramCounterUnit.requestStartProgram(command[1], command[2]);
                }
                break;
            case 0x03:
                if(STARTED_FLAG){
                    RAMUnit.writeData(command[1], command[2], command[3]);
                }
                break;
            case 0x04:
                if (STARTED_FLAG) {
                    ROMUnit.requestWriteData(command[1], command[2], command[3]);
                }
                break;
            case 0x05:
                if(STARTED_FLAG){
                    ScreenUnit.writePixel(command[1], command[2], command[3]);
                }
                break;
            case 0x06:
                if(STARTED_FLAG){
                    ArithmeticLogicUnit.evaluate(command[1], command[2], command[3]);
                }
                break;
            case 0x07: // NO OP
                break;
            case 0x08:
                if(STARTED_FLAG){
                    ScreenUnit.clearScreen();
                }
                break;
            case 0x09:
                if(STARTED_FLAG){
                    // first two command bytes are the location of the first piece of pixel data in RAM
                    // the third command byte is the size of the pixel data (in words/DoubleBytes) (so 7 pixels at X:0 Y:0 would be ScreenUnit.pixelDataDump(0x00, 0x00, 0x07) )
                    ScreenUnit.pixelDataDump(command[1], command[2], command[3]);
                }
        }
    }

    public static DoubleByte readFromMemory(byte op){
        switch(op){
            case 0x00:
                return ProgramCounterUnit.readData(); // Equivalent to ROM read
            case 0x01:
                return ArithmeticLogicUnit.readData(); // Read result of ALU
            case 0x02:
                return RAMUnit.readData(); // Read from RAM
            default:
                throw new IllegalArgumentException("Invalid operation byte: " + op);
        }
    }

    public static void shutdownProtocol(){
        // write all data to file
        ROMUnit.saveToFile();
        STARTED_FLAG = false;
        // add any other shutdown protocols here
    }

    public static void startupProtocol(){
        // read all data from file
        ROMUnit.loadFromFile();
        STARTED_FLAG = true;
        // add any other startup protocols here
    }
}
