package src;
import java.io.File;
import java.util.Scanner;

import java.util.HashMap;

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
    
    // MISC VARS
    private static int tokenNumber = 0;
    private static boolean simMode = true;
    private static HashMap<String, File> dependencies = new HashMap<String, File>();

    private static void interpretEBF(File eBFFile) throws Exception{
        Scanner sc = new Scanner(eBFFile);
        String eBFCode = "";
        while(sc.hasNextLine()){ eBFCode += sc.nextLine() + " "; }
        sc.close();

        String[] eBFTokens = eBFCode.split(" ");
        for(int i = 0; i < eBFTokens.length; i++){
            switch(eBFTokens[i]){
                case "+":
                    incrementPointerValue();
                    break;
                case "-":
                    decrementPointerValue();
                    break;
                case ">":
                    movePointerRight();
                    break;
                case "<":
                    movePointerLeft();
                    break;
                case "[":
                    if(RAM[pointerX][pointerY].isZero()) {
                        int openBrackets = 0;
                        i++;
                        while(openBrackets > 0 || !eBFTokens[i].equals("]")) { //TODO: Fix IndexOutOfBoundsException occurring here
                            if(eBFTokens[i].equals("[")) {
                                openBrackets++;
                            } else if(eBFTokens[i].equals("]")) {
                                openBrackets--;
                            } else {
                                int numberOfTokensRemainingInProgram = eBFTokens.length - (i + 3);
                                if(numberOfTokensRemainingInProgram > 0){ numberOfTokensRemainingInProgram = 1; }
                                switch(numberOfTokensRemainingInProgram) {
                                    case 1:
                                        i += runBFInstruction(new String[]{eBFTokens[i], eBFTokens[i+1], eBFTokens[i+2], eBFTokens[i+3]});
                                        break;
                                    case 0:
                                        i += runBFInstruction(new String[]{eBFTokens[i], eBFTokens[i+1], eBFTokens[i+2]});
                                        break;
                                    case -1:
                                        i += runBFInstruction(new String[]{eBFTokens[i], eBFTokens[i+1]});
                                        break;
                                    case -2:
                                        i += runBFInstruction(new String[]{eBFTokens[i]});
                                        break;
                                    default:
                                        break;
                                }
                            }
                            i++;
                        }
                    }
                    break;
                case "]":
                    if(!RAM[pointerX][pointerY].isZero()) {
                        int closeBrackets = 0;
                        i--;
                        while(closeBrackets > 0 || !eBFTokens[i].equals("[")) {
                            if(eBFTokens[i].equals("]")) {
                                closeBrackets++;
                            } else if(eBFTokens[i].equals("[")) {
                                closeBrackets--;
                            } else if(eBFTokens[i].equals("DPND")) {
                                throw new Exception("Looped Dependency Set Call" + tokenNumber);  
                            } else {
                                int numberOfTokensRemainingInProgram = eBFTokens.length - (i + 3);
                                if(numberOfTokensRemainingInProgram > 0){ numberOfTokensRemainingInProgram = 1; }
                                switch(numberOfTokensRemainingInProgram) {
                                    case 1:
                                        i += runBFInstruction(new String[]{eBFTokens[i], eBFTokens[i+1], eBFTokens[i+2], eBFTokens[i+3]});
                                        break;
                                    case 0:
                                        i += runBFInstruction(new String[]{eBFTokens[i], eBFTokens[i+1], eBFTokens[i+2]});
                                        break;
                                    case -1:
                                        i += runBFInstruction(new String[]{eBFTokens[i], eBFTokens[i+1]});
                                        break;
                                    case -2:
                                        i += runBFInstruction(new String[]{eBFTokens[i]});
                                        break;
                                    default:
                                        break;
                                }
                            }
                            i--;
                        }
                    }
                    break;
                case ",":
                    RAM[pointerX][pointerY] = DoubleByte.convertToDoubleByte(pointerValue);
                    break;
                case "=":
                    System.out.print((char) RAM[pointerX][pointerY].convertToInt());
                    break;
                case ".":
                    RAM[pointerX][pointerY].setHighByte((byte) System.in.read());
                    RAM[pointerX][pointerY].setLowByte((byte) System.in.read());
                    break;
                case ">>":
                    stack[stackPointer] = DoubleByte.convertToDoubleByte(pointerValue);
                    incrementStackPointer();
                    break;
                case "<<":
                    pointerValue = stack[stackPointer].convertToInt();
                    decrementStackPointer();
                    break;
                case "DPND":
                    if(eBFTokens[i+1].contains(".")){ dependencies.put(eBFTokens[i+2], new File(eBFTokens[i+1])); i += 2; }
                    // else if(inSimMode()){ ePUx16Sim.loadDependency(eBFTokens[i+1], eBFTokens[i+2], eBFTokens[i+3]); i += 3; }
                    break;
                case "%":
                    if(dependencies.containsKey(eBFTokens[i+1])){
                        if(dependencies.get(eBFTokens[i+1]).getName().endsWith(".ebin")){
                            interpretEBIN(dependencies.get(eBFTokens[i+1]));
                            i++;
                        } else if(dependencies.get(eBFTokens[i+1]).getName().endsWith(".ebf")){
                            interpretEBF(dependencies.get(eBFTokens[i+1]));
                            i++;
                        } else {
                            System.out.println("Error: Invalid Dependency File Type");
                        }
                    }
                    // else if(inSimMode()){ ePUx16Sim.runDependency(eBFTokens[i+1]); i++;}
                    break;
                // case "$":
                //     if(inSimMode()){ ePUx16Sim.SystemCall(eBFTokens[i+1], eBFTokens[i+2], eBFTokens[i+3]); i += 3;} //TODO: implement Sys Call
                //     break;
                case "END":
                    break;
                default:
                    throw new UnrecognizedTokenException("Unrecognized Token: " + eBFTokens[i] + " at token number: " + tokenNumber);
            }
            tokenNumber++;
        }
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
                    if (RAM[pointerX][pointerY].isZero()) {
                        int openBrackets = 0;
                        i++;
                        while(openBrackets > 0 || !eBinTokens[i].equals("0110")) {
                            if(eBinTokens[i].equals("0101")) {
                                openBrackets++;
                            } else if(eBinTokens[i].equals("0110")) {
                                openBrackets--;
                            } else {
                                int numberOfTokensRemainingInProgram = eBinTokens.length - (i + 3);
                                if(numberOfTokensRemainingInProgram > 0){ numberOfTokensRemainingInProgram = 1; }
                                switch(numberOfTokensRemainingInProgram) {
                                    case 1:
                                        i += runBinInstruction(new String[]{eBinTokens[i], eBinTokens[i+1], eBinTokens[i+2], eBinTokens[i+3]});
                                        break;
                                    case 0:
                                        i += runBinInstruction(new String[]{eBinTokens[i], eBinTokens[i+1], eBinTokens[i+2]});
                                        break;
                                    case -1:
                                        i += runBinInstruction(new String[]{eBinTokens[i], eBinTokens[i+1]});
                                        break;
                                    case -2:
                                        i += runBinInstruction(new String[]{eBinTokens[i]});
                                        break;
                                    default:
                                        break;
                                }
                            }
                            i++;
                        }
                    }
                    break;
                case "0110":
                    if (!RAM[pointerX][pointerY].isZero()) {
                        int closeBrackets = 0;
                        i--;
                        while(closeBrackets > 0 || !eBinTokens[i].equals("0101")) {
                            if(eBinTokens[i].equals("0110")) {
                                closeBrackets++;
                            } else if(eBinTokens[i].equals("0101")) {
                                closeBrackets--;
                            } else if(eBinTokens[i].equals("1011")) {
                                throw new Exception("Looped Dependency Set Call" + tokenNumber);  
                            } else {
                                int numberOfTokensRemainingInProgram = eBinTokens.length - (i + 3);
                                if(numberOfTokensRemainingInProgram > 0){ numberOfTokensRemainingInProgram = 1; }
                                switch(numberOfTokensRemainingInProgram) {
                                    case 1:
                                        i += runBinInstruction(new String[]{eBinTokens[i], eBinTokens[i+1], eBinTokens[i+2], eBinTokens[i+3]});
                                        break;
                                    case 0:
                                        i += runBinInstruction(new String[]{eBinTokens[i], eBinTokens[i+1], eBinTokens[i+2]});
                                        break;
                                    case -1:
                                        i += runBinInstruction(new String[]{eBinTokens[i], eBinTokens[i+1]});
                                        break;
                                    case -2:
                                        i += runBinInstruction(new String[]{eBinTokens[i]});
                                        break;
                                    default:
                                        break;
                                }
                            }
                            i--;
                        }
                    }
                    break;
                case "0111":
                    RAM[pointerX][pointerY] = DoubleByte.convertToDoubleByte(pointerValue);
                    break;
                case "1111":
                    System.out.print((char) RAM[pointerX][pointerY].convertToInt());
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
                    if(eBinTokens[i+1].contains(".")){ dependencies.put(eBinTokens[i+2], new File(eBinTokens[i+1])); i += 2; }
                    //else if(inSimMode()){ ePUx16Sim.loadDependency(eBinTokens[i+1], eBinTokens[i+2], eBinTokens[i+3]); i += 3; }
                    break;
                case "1100":
                    if(dependencies.containsKey(eBinTokens[i+1])){
                        if(dependencies.get(eBinTokens[i+1]).getName().endsWith(".ebin")){
                            interpretEBIN(dependencies.get(eBinTokens[i+1]));
                            i++;
                        } else if(dependencies.get(eBinTokens[i+1]).getName().endsWith(".ebf")){
                            interpretEBF(dependencies.get(eBinTokens[i+1]));
                            i++;
                        } else {
                            System.out.println("Error: Invalid Dependency File Type");
                        }
                    }
                    //else if(inSimMode()){ ePUx16Sim.runDependency(eBinTokens[i+1]); i++;}
                    break;
                // case "1101":
                //     if(inSimMode()){ ePUx16Sim.SystemCall(eBinTokens[i+1], eBinTokens[i+2], eBinTokens[i+3]); i += 3;} //TODO: implement Sys Call
                //     break;
                case "1110":
                    break;
                default:
                    throw new UnrecognizedTokenException("Unrecognized Token: " + eBinTokens[i] + " at token number: " + tokenNumber);
            }
            tokenNumber++;
        }
    }

    public static void main(String[] args){
        initializeRAM();

        if(args.length == 0){
            System.out.println("Usage: java -jar eBFInterpreter.jar <flag> <eBin/eBF File>");
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
            } catch(Exception e){ e.printStackTrace(); }
        }
        simMode = false;
    }

    private static void initializeRAM(){
        for(int i = 0; i < 16; i++){
            for(int j = 0; j < 16; j++){
                RAM[i][j] = new DoubleByte((byte) 0x0000, (byte) 0x0000);
            }
        }
    }

    private static boolean inSimMode(){ return simMode; }

    private static int runBFInstruction(String[] instructions) throws Exception {
        int tokensRead = 0;
        switch(instructions[0]){
            case "+":
                incrementPointerValue();
                break;
            case "-":
                decrementPointerValue();
                break;
            case ">":
                movePointerRight();
                break;
            case "<":
                movePointerLeft();
                break;
            case ",":
                RAM[pointerX][pointerY] = DoubleByte.convertToDoubleByte(pointerValue);
                break;
            case "=":
                System.out.print((char) RAM[pointerX][pointerY].convertToInt());
                break;
            case ".":
                RAM[pointerX][pointerY].setHighByte((byte) System.in.read());
                RAM[pointerX][pointerY].setLowByte((byte) System.in.read());
                break;
            case ">>":
                stack[stackPointer] = DoubleByte.convertToDoubleByte(pointerValue);
                incrementStackPointer();
                break;
            case "<<":
                pointerValue = stack[stackPointer].convertToInt();
                decrementStackPointer();
                break;
            case "%":
                if(dependencies.containsKey(instructions[1])){
                    if(dependencies.get(instructions[1]).getName().endsWith(".ebin")){
                        interpretEBIN(dependencies.get(instructions[1]));
                        tokensRead = 1;
                    } else if(dependencies.get(instructions[1]).getName().endsWith(".ebf")){
                        interpretEBF(dependencies.get(instructions[1]));
                        tokensRead = 1;
                    } else {
                        System.out.println("Error: Invalid Dependency File Type");
                    }
                }
                // else if(inSimMode()){ ePUx16Sim.runDependency(instructions[1]); tokensRead = 1;}
                break;
            // case "$":
            //     if(inSimMode()){ ePUx16Sim.SystemCall(instructions[1], instructions[2], instructions[3]); }
            //     tokensRead = 3;
            //     break;
            case "END":
                break;
            default:
                break;
        }
        tokenNumber++;
        return tokensRead;
    }

    private static int runBinInstruction(String[] instructions) throws Exception {
        int tokensRead = 0;
        switch(instructions[0]){
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
            case "1111":
                System.out.print((char) RAM[pointerX][pointerY].convertToInt());
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
                if(dependencies.containsKey(instructions[1])){
                    if(dependencies.get(instructions[1]).getName().endsWith(".ebin")){
                        interpretEBIN(dependencies.get(instructions[1]));
                        tokensRead = 1;
                    } else if(dependencies.get(instructions[1]).getName().endsWith(".ebf")){
                        interpretEBF(dependencies.get(instructions[1]));
                        tokensRead = 1;
                    } else {
                        System.out.println("Error: Invalid Dependency File Type");
                    }
                }
                // else if(inSimMode()){ ePUx16Sim.runDependency(instructions[1]); tokensRead = 1;}
                break;
            // case "1101":
            //     if(inSimMode()){ ePUx16Sim.SystemCall(instructions[1], instructions[2], instructions[3]); }
            //     tokensRead = 3;
            //     break;
            case "1110":
                break;
            default:
                break;
        }
        tokenNumber++;
        return tokensRead;
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