package ePUx16Sim;

import eBF.DoubleByte;

public class ControlUnit {
    private static boolean STARTED_FLAG = false;

    public static void commandUnit(byte[] command){ // command -> { min size: 1 | max size: 5 }
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
                    RAMUnit.writeData(command[1], command[2], new DoubleByte(command[3], command[4]));
                }
                break;
            case 0x04:
                if (STARTED_FLAG) {
                    ROMUnit.requestWriteData(command[1], command[2], new DoubleByte(command[3], command[4]));
                }
                break;
            case 0x05:
                if(STARTED_FLAG){
                    ScreenUnit.writePixel(command[1], command[2], new DoubleByte(command[3], command[4]));
                }
                break;
            case 0x06:
                if(STARTED_FLAG){
                    ArithmeticLogicUnit.evaluate(command[1], command[2], command[3]);
                }
                break;
            case 0x07: // NO OP
                break;
            case 0x08: // clear screen
                if(STARTED_FLAG){
                    ScreenUnit.clearScreen();
                }
                break;
            case 0x09: // dump pixel data
                if(STARTED_FLAG){
                    ScreenUnit.pixelDataDump(command[1], command[2], new DoubleByte(command[3], command[4]));
                }
            case 0x10: // set protected memory
                if(STARTED_FLAG){
                    ROMUnit.setProtectedMemory(command[1], command[2], new DoubleByte(command[3], command[4]));
                }
        }
    }

    public static DoubleByte readFromMemory(byte op, byte x, byte y){
        switch(op){
            case 0x00:
                return ProgramCounterUnit.readData(x, y); // Equivalent to ROM read
            case 0x01:
                return ArithmeticLogicUnit.readData(x, y); // Read result of ALU
            case 0x02:
                return RAMUnit.readData(x, y); // Read from RAM
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
        ScreenUnit.intializeScreen();
        STARTED_FLAG = true;
        // add any other startup protocols here
    }
}
