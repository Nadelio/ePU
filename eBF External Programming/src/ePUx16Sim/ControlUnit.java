package ePUx16Sim;

import eBF.DoubleByte;

public class ControlUnit {
    private static boolean STARTED_FLAG = false;

    public static void commandUnit(byte[] command){
        switch(command[0]){
            case 0x00:
                System.exit(0);
                break;
            case 0x01:
                STARTED_FLAG = true;
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
                    ScreenUnit.writePixels(command[1], command[2], command[3]);
                }
                break;
            case 0x06:
                if(STARTED_FLAG){
                    ArithmeticLogicUnit.evaluate(command[1], command[2], command[3]);
                }
                break;
            case 0x07: // NO OP
                break;
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
        }
    }
}
