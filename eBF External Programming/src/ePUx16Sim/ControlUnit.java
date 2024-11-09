package ePUx16Sim;

import java.io.IOException;

public class ControlUnit {
    private static boolean STARTED_FLAG = false;

    public static void commandUnit(UnsignedByte[] command) throws Exception { // command -> { min size: 1 | max size: 5 }
        switch(command[0].value){
            case 0: // stop computer
                shutdownProtocol();
                System.exit(0);
                break;
            case 1: // start computer
                startupProtocol();
                break;
            case 2: // load and start program
                if(STARTED_FLAG){
                    ProgramCounterUnit.requestStartProgram(command[1], command[2], Word.convertToWord(Registers.findSize(command[1], command[2])));
                }
                break;
            case 3: // write data to RAM
                if(STARTED_FLAG){
                    RAMUnit.writeData(command[1], command[2], new Word(command[3], command[4]));
                }
                break;
            case 4: // request write data to ROM
                if (STARTED_FLAG) {
                    ROMUnit.requestWriteData(command[1], command[2], new Word(command[3], command[4]));
                }
                break;
            case 5: // write pixel to screen
                if(STARTED_FLAG){
                    ScreenUnit.writePixel(command[1], command[2], new Word(command[3], command[4]));
                }
                break;
            case 6: // math operations
                if(STARTED_FLAG){
                    ArithmeticLogicUnit.evaluate(command[1], command[2], command[3]);
                }
                break;
            case 7: // NO OP
                break;
            case 8: // clear screen
                if(STARTED_FLAG){
                    ScreenUnit.clearScreen();
                }
                break;
            case 9: // dump pixel data
                if(STARTED_FLAG){
                    ScreenUnit.pixelDataDump(command[1], command[2], new Word(command[3], command[4]));
                }
                break;
            case 10: // set protected memory
                if(STARTED_FLAG){
                    ROMUnit.setProtectedMemory(command[1], command[2], new Word(command[3], command[4]));
                }
                break;
            case 11: // read from PC
                if(STARTED_FLAG){
                    EmbeddedeBFInterpreter.setPointerValue(ProgramCounterUnit.readData(command[1], command[2]));
                }
                break;
            case 12: // read from ALU
                if(STARTED_FLAG){
                    EmbeddedeBFInterpreter.setPointerValue(ArithmeticLogicUnit.readData());
                }
                break;
            case 13:
                if(STARTED_FLAG){ // request write data dump to ROM
                    ROMUnit.requestWriteDataHeap(command[1], command[2], new Word(command[3], command[4]));
                }
                break;
            case 14:
                if(STARTED_FLAG){ // write to terminal
                    System.out.print((char) (EmbeddedeBFInterpreter.getPointerValue()));
                }
                break;
        }
    }

    public static void shutdownProtocol(){
        System.out.println("| Stopping computer |");
        // write all data to file
        try { ROMUnit.saveToFile(); } catch (IOException e) { e.printStackTrace(); }
        STARTED_FLAG = false;
        // add any other shutdown protocols here
    }

    public static void startupProtocol() throws IOException{
        System.out.println("| Starting computer |");
        // read all data from file
        ROMUnit.loadFromFile();
        ScreenUnit.intializeScreen();
        RAMUnit.initializeRAM();
        STARTED_FLAG = true;
        // add any other startup protocols here
    }
}
