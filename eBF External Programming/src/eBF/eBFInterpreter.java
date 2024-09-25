package eBF;
import java.io.File;
import java.util.Scanner;

import ePUx16Sim.ControlUnit;
import ePUx16Sim.RAMUnit;

import java.util.HashMap;

public class eBFInterpreter {

    // STACK OPERATIONS
    private static DoubleByte[] stack = new DoubleByte[256];
    private static int stackPointer = 0;

    // RAM OPERATIONS
    private static int pointerX = 0;
    private static int pointerY = 0;
    private static int pointerValue = 0;
    private static final int maxPointerValue = (int) Math.pow(2, 8) - 1;
    
    // MISC VARS
    private static int tokenNumber = 0;
    private static HashMap<String, File> dependencies = new HashMap<String, File>();

    @Deprecated
    private static void interpretEBF(File eBFFile) throws Exception{
        Scanner sc = new Scanner(eBFFile);
        String eBFCode = "";
        while(sc.hasNextLine()){ eBFCode += sc.nextLine() + " "; }
        sc.close();

        int c = 0;

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
                    if(RAMUnit.RAM[pointerX][pointerY].isZero()) {
                        i++;
                        while(c > 0 || !eBFTokens[i].equals("]")) {
                            
                            if(eBFTokens[i].equals("[")) {
                                c++;
                            } else if(eBFTokens[i].equals("]")) {
                                c--;
                            } else if(eBFTokens[i].equals("DPND")) {
                                throw new Exception("Looped Dependency Set Call" + tokenNumber);  
                            }
                            i++;
                        }
                    }
                    break;
                case "]":
                    if(!RAMUnit.RAM[pointerX][pointerY].isZero()) {
                        i--;
                        while(c > 0 || !eBFTokens[i].equals("[")) {
                            
                            if(eBFTokens[i].equals("]")) {
                                c++;
                            } else if(eBFTokens[i].equals("[")) {
                                c--;
                            } else if(eBFTokens[i].equals("DPND")) {
                                throw new Exception("Looped Dependency Set Call" + tokenNumber);  
                            }
                            i--;
                        }
                    }
                    break;
                case ",":
                    RAMUnit.RAM[pointerX][pointerY] = DoubleByte.convertToDoubleByte(pointerValue);
                    break;
                case "=": // write to terminal
                    System.out.print((char) (RAMUnit.RAM[pointerX][pointerY].convertToInt() + 32));
                    break;
                case ".":
                    RAMUnit.RAM[pointerX][pointerY].setHighByte((byte) System.in.read());
                    RAMUnit.RAM[pointerX][pointerY].setLowByte((byte) System.in.read());
                    break;
                case ">>":
                    incrementStackPointer();
                    stack[stackPointer] = DoubleByte.convertToDoubleByte(pointerValue);
                    pointerValue = 0;
                    break;
                case "<<":
                    pointerValue = stack[stackPointer].convertToInt();
                    stack[stackPointer] = DoubleByte.convertToDoubleByte(0);
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
                case "'": // read from RAM
                    pointerValue = RAMUnit.RAM[pointerX][pointerY].convertToInt();
                    break;
                // case "$": // sys call
                //     if(inSimMode()){ ePUx16Sim.SystemCall(eBFTokens[i+1], eBFTokens[i+2], eBFTokens[i+3]); i += 3;} //TODO: implement Sys Call
                //     break;
                case "END":
                    break;
                default:
                    throw new UnrecognizedTokenException("Unrecognized Token: " + eBFTokens[i] + " at token number: " + tokenNumber);
            }
            tokenNumber++;
            debugStats(DEBUG_FLAG, eBFTokens[i]);
        }
    }

    // TODO: Update the interpreter to handle bytes instead of 4 bit tokens
    public static void interpretEBIN(File eBinaryFile) throws Exception{
        Scanner sc = new Scanner(eBinaryFile);
        String eBinCode = "";
        while(sc.hasNextLine()){ eBinCode += sc.nextLine() + " "; }
        sc.close();

        int c = 0;

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
                    if (RAMUnit.RAM[pointerX][pointerY].isZero()) {
                        i++;
                        while(c > 0 || !eBinTokens[i].equals("0110")) {
                            if(eBinTokens[i].equals("0101")) {
                                c++;
                            } else if(eBinTokens[i].equals("0110")) {
                                c--;
                            } else if(eBinTokens[i].equals("1011")) {
                                throw new Exception("Looped Dependency Set Call" + tokenNumber);  
                            }
                            i++;
                        }
                    }
                    break;
                case "0110":
                    if (!RAMUnit.RAM[pointerX][pointerY].isZero()) {
                        i--;
                        while(c > 0 || !eBinTokens[i].equals("0101")) {
                            
                            if(eBinTokens[i].equals("0110")) {
                                c++;
                            } else if(eBinTokens[i].equals("0101")) {
                                c--;
                            } else if(eBinTokens[i].equals("1011")) {
                                throw new Exception("Looped Dependency Set Call" + tokenNumber);  
                            }
                            i--;
                        }
                    }
                    break;
                case "0111":
                    RAMUnit.RAM[pointerX][pointerY] = DoubleByte.convertToDoubleByte(pointerValue);
                    break;
                case "1101": // read from RAM
                    pointerValue = RAMUnit.RAM[pointerX][pointerY].convertToInt();
                    break;
                case "1000":
                    RAMUnit.RAM[pointerX][pointerY].setHighByte((byte) System.in.read());
                    RAMUnit.RAM[pointerX][pointerY].setLowByte((byte) System.in.read());
                    break;
                case "1001":
                    incrementStackPointer();
                    stack[stackPointer] = DoubleByte.convertToDoubleByte(pointerValue);
                    pointerValue = 0;
                    break;
                case "1010":
                    pointerValue = stack[stackPointer].convertToInt();
                    stack[stackPointer] = DoubleByte.convertToDoubleByte(0);
                    decrementStackPointer();
                    break;
                case "1011":
                    // TODO: implement dependency loading
                    if(eBinTokens[i+1].contains(".")){ dependencies.put(eBinTokens[i+2], new File(eBinTokens[i+1])); i += 2; }
                    break;
                case "1100":
                    // TODO: implement dependency running
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
                    break;
                case "1110": // sys call
                    ControlUnit.commandUnit(toByte(new String[]{ eBinTokens[i+1], eBinTokens[i+2], eBinTokens[i+3], eBinTokens[i+4], eBinTokens[i+5] }));
                    i += 5;
                    break;
                case "1111": // END
                    break;
                default:
                    throw new UnrecognizedTokenException("Unrecognized Token: " + eBinTokens[i] + " at token number: " + tokenNumber);
            }
            tokenNumber++;
            debugStats(DEBUG_FLAG, eBinTokens[i]);
        }
    }

    private static boolean DEBUG_FLAG = false;

    @Deprecated
    public static void main(String[] args){
        initializeRAM();
        initializeStack();

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
                if(args[0].contains("-d")){
                    DEBUG_FLAG = true;
                }
                if(args[0].contains("--eBF")){
                    interpretEBF(new File(args[1]));
                } else if(args[0].contains("--eBin")){
                    interpretEBIN(new File(args[1]));
                } else if(args[0].equals("--help") || args[0].equals("-h")){
                    System.out.println("Run eBF file: --eBF <fileName>.ebf\nRun eBin file: --eBin <fileName>.ebin\nHelp: --help or -h");
                } else {
                    System.out.println("Error: Invalid Flag");
                    System.exit(0);
                }
            } catch(Exception e){ e.printStackTrace(); }
        }
    }

    private static byte[] toByte(String[] s){
        byte[] b = new byte[5];
        for(int i = 0; i < 5; i++){
            b[i] = (byte) Integer.parseInt(s[i], 2);
        }
        return b;
    }

    private static void initializeRAM(){
        for(int i = 0; i < 255; i++){
            for(int j = 0; j < 255; j++){
                RAMUnit.RAM[i][j] = new DoubleByte((byte) 0x0000, (byte) 0x0000);
            }
        }
    }

    public static void initializeStack(){
        for(int i = 0; i < stack.length; i++){
            stack[i] = new DoubleByte((byte) 0x0000, (byte) 0x0000);
        }
    }

    private static void debugStats(boolean flag, String token){
        if(flag){
            System.out.println("Pointer Value: " + pointerValue);
            System.out.println("Pointer X: " + pointerX);
            System.out.println("Pointer Y: " + pointerY);
            System.out.println("Stack Pointer: " + stackPointer);
            System.out.println("Stack Value: " + stack[stackPointer].convertToInt());
            System.out.println("RAM Value: " + RAMUnit.RAM[pointerX][pointerY].convertToInt());
            System.out.println("Token Number: " + tokenNumber);
            System.out.println("Token: " + token);
            System.out.println();
        }
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
        if (pointerX == 255) {
            pointerX = 0;
            pointerY++;
            if (pointerY == 255) {
                pointerY = 0;
                pointerX = 0;
            }
        }
    }

    private static void movePointerLeft() {
        pointerX--;
        if (pointerX == -1) {
            pointerX = 255;
            pointerY--;
            if (pointerY == -1) {
                pointerY = 255;
                pointerX = 255;
            }
        }
    }

    public static void setPointerValue(DoubleByte value){ pointerValue = value.convertToInt(); }
}
