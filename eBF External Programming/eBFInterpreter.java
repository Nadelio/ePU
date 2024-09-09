import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class eBFInterpreter {

    // STACK OPERATIONS
    private static DoubleByte[] stack = new DoubleByte[256];
    private static int stackPointer = 0;

    // RAM OPERATIONS
    private static int pointerX = 0;
    private static int pointerY = 0;
    private static int pointerValue = 0;
    private static DoubleByte[][] RAM = new DoubleByte[16][16];
    private static final int maxPointerValue = (int) Math.pow(2, 16) - 1;
    
    // INSTRUCTION POINTER
    private static int instructionPointer = 0;
    
    // MISC VARS
    private static int tokenNumber = 0;
    private static boolean simMode = true;

    private static void interpretEBF(File eBFFile) {
        //TODO: implement eBF interpreter
    }

    private static void interpretEBIN(File eBinaryFile) throws Exception{
        Scanner sc = new Scanner(eBinaryFile);
        String eBinCode = "";
        while(sc.hasNextLine()){ eBinCode += sc.nextLine() + " "; }
        sc.close();

        String[] eBinTokens = eBinCode.split(" ");
        for(int i = 0; i < eBinTokens.length; i++){
            switch(eBinTokens[i]){
                case "0001":
                    incrementPointerValue();
                    break;
                case "0010":
                    decrementPointerValue();
                    break;
                case "0011":
                    movePointerRight();
                    break;
                case "0100":
                    movePointerLeft();
                    break;
                case "0101":
                    // '[' command: Jump to the matching ']' if the current cell value is zero
                    if (RAM[pointerX][pointerY].isZero()) {
                        int openBrackets = 1;
                        while(openBrackets != 0) {
                            instructionPointer++;
                            if(eBinTokens[instructionPointer].equals("0101")) {
                                openBrackets++;
                            } else if(eBinTokens[instructionPointer].equals("0110")) {
                                openBrackets--;
                            } else {
                                runInstruction(eBinTokens[instructionPointer]); // switch case with all the instructions except '[' and ']'
                            }
                        }
                    }
                    break;
                case "0110":
                    // ']' command: Jump back to the matching '[' if the current cell value is non-zero
                    if (!RAM[pointerX][pointerY].isZero()) {
                        int closeBrackets = 1;
                        while(closeBrackets != 0) {
                            instructionPointer--;
                            if(eBinTokens[instructionPointer].equals("0101")) {
                                closeBrackets--;
                            } else if(eBinTokens[instructionPointer].equals("0110")) {
                                closeBrackets++;
                            } else if(eBinTokens[instructionPointer].equals("1011")) {
                                throw new Exception("Looped Dependency Set Call" + tokenNumber);  
                            } else {
                                runInstruction(eBinTokens[instructionPointer]); // switch case with all the instructions except '[' and ']'
                            }
                        }
                    }
                    break;
                case "0111":
                    RAM[pointerX][pointerY] = DoubleByte.convertToDoubleByte(pointerValue);
                    break;
                case "1000":
                    RAM[pointerX][pointerY].setHighByte((byte) System.in.read());
                    RAM[pointerX][pointerY].setLowByte((byte) System.in.read());
                    break;
                case "1001":
                    stack[stackPointer] = DoubleByte.convertToDoubleByte(pointerValue);
                    incrementStackPointer();
                    break;
                case "1010":
                    pointerValue = stack[stackPointer].convertToInt();
                    decrementStackPointer();
                    break;
                case "1011":
                    // Set Dependency //TODO: implement Dependency Set
                    break;
                case "1100":
                    // Call Dependency Code //TODO: implement Dependency Call
                    break;
                case "1101":
                    if(inSimMode()){ ePUx16Sim.SystemCall(eBinTokens[i]); } //TODO: implement Sys Call
                    break;
                case "1110":
                    System.out.println("[END OF PROGRAM]");
                    break;
                default:
                    break;
            }
            tokenNumber++;
        }
    }

    public static void main(String[] args){
        if(args.length == 0){
            System.out.println("Usage: java -jar eBinInterpreter.jar <flag> <eBin/eBF File>");
            System.exit(0);
        } else if(args.length == 1){
            System.out.println("Error: Missing eBin/eBF File or Flag");
            System.exit(0);
        } else if(args.length > 2){
            System.out.println("Error: Too many arguments");
            System.exit(0);
        } else {
            try{
                if(args[0].equals("--eBF")){
                    interpretEBF(new File(args[1]));
                } else if(args[0].equals("--eBin")){
                    interpretEBIN(new File(args[1]));
                } else if(args[0].equals("--help") || args[0].equals("-h")){
                    System.out.println("Run eBF file: --eBF <fileName>.ebf\nRun eBin file: --eBin <fileName>.ebin\nHelp: --help or -h");
                } else {
                    System.out.println("Error: Invalid Flag");
                    System.exit(0);
                }
            }catch(Exception e){ e.printStackTrace(); }
        }
        simMode = false;
    }

    private static boolean inSimMode(){ return simMode; }

    private static void runInstruction(String instruction) throws IOException {
        switch(instruction){
            case "0001":
                incrementPointerValue();
                break;
            case "0010":
                decrementPointerValue();
                break;
            case "0011":
                movePointerRight();
                break;
            case "0100":
                movePointerLeft();
                break;
            case "0111":
                RAM[pointerX][pointerY] = DoubleByte.convertToDoubleByte(pointerValue);
                break;
            case "1000":
                RAM[pointerX][pointerY].setHighByte((byte) System.in.read());
                RAM[pointerX][pointerY].setLowByte((byte) System.in.read());
                break;
            case "1001":
                stack[stackPointer] = DoubleByte.convertToDoubleByte(pointerValue);
                incrementStackPointer();
                break;
            case "1010":
                pointerValue = stack[stackPointer].convertToInt();
                decrementStackPointer();
                break;
            case "1100":
                //TODO: Call Dependency Code
                break;
            case "1101":
                if(inSimMode()){ ePUx16Sim.SystemCall(instruction); } //TODO: implement Sys Call
                break;
            case "1110":
                System.out.println("[END OF PROGRAM]");
                break;
            default:
                break;
        }
        tokenNumber++;
    }

    private static void incrementStackPointer() {
        stackPointer++;
        if (stackPointer > 255) {
            stackPointer = 0;
        }
    }

    private static void decrementStackPointer() {
        stackPointer--;
        if (stackPointer < 0) {
            stackPointer = 255;
        }
    }

    private static void incrementPointerValue() {
        pointerValue++;
        if (pointerValue > maxPointerValue) {
            pointerValue = 0;
        }
    }

    private static void decrementPointerValue() {
        pointerValue--;
        if (pointerValue < 0) {
            pointerValue = maxPointerValue;
        }
    }

    private static void movePointerRight() {
        pointerX++;
        if (pointerX == 15) {
            pointerX = 0;
            pointerY++;
            if (pointerY == 15) {
                pointerY = 0;
                pointerX = 0;
            }
        }
    }

    private static void movePointerLeft() {
        pointerX--;
        if (pointerX == -1) {
            pointerX = 15;
            pointerY--;
            if (pointerY == -1) {
                pointerY = 15;
                pointerX = 15;
            }
        }
    }
}