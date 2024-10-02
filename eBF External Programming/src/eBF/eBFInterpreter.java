package eBF;

import java.io.File;
import java.util.Scanner;

import java.util.HashMap;

public class eBFInterpreter {

    // STACK OPERATIONS
    private static DoubleByte[] stack = new DoubleByte[256];
    private static int stackPointer = 0;

    // TAPE OPERATIONS
    private static int pointerX = 0;
    private static int pointerY = 0;
    private static int pointerValue = 0;
    private static DoubleByte[][] Tape = new DoubleByte[256][256];
    private static final int maxPointerValue = (int) Math.pow(2, 8) - 1;
    
    // MISC VARS
    private static int tokenNumber = 0;
    private static HashMap<String, File> dependencies = new HashMap<String, File>();
    private static HashMap<String, int[]> labels = new HashMap<String, int[]>();

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
                    if(Tape[pointerX][pointerY].isZero()) {
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
                    if(!Tape[pointerX][pointerY].isZero()) {
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
                    Tape[pointerX][pointerY] = DoubleByte.convertToDoubleByte(pointerValue);
                    break;
                case "=": // write to terminal
                    System.out.print((char) (Tape[pointerX][pointerY].convertToInt() + 32));
                    break;
                case ".":
                    Tape[pointerX][pointerY].setHighByte((byte) System.in.read());
                    Tape[pointerX][pointerY].setLowByte((byte) System.in.read());
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
                    break;
                case "'": // read from Tape
                    pointerValue = Tape[pointerX][pointerY].convertToInt();
                    break;
                case "#":
                    labels.put(eBFTokens[i+1], new int[]{ pointerX, pointerY });
                    i++;
                    break;
                case "@":
                    if(labels.containsKey(eBFTokens[i+1])){
                        pointerX = labels.get(eBFTokens[i+1])[0];
                        pointerY = labels.get(eBFTokens[i+1])[1];
                    }
                    i++;
                    break;
                case "END":
                    break;
                default:
                    throw new UnrecognizedTokenException("Unrecognized Token: " + eBFTokens[i] + " at token number: " + tokenNumber);
            }
            tokenNumber++;
            debugStats(DEBUG_FLAG, eBFTokens[i]);
        }
    }

    private static void interpretEBIN(File eBinaryFile) throws Exception{
        Scanner sc = new Scanner(eBinaryFile);
        String eBinCode = "";
        while(sc.hasNextLine()){ eBinCode += sc.nextLine() + " "; }
        sc.close();

        int c = 0;

        String[] eBinTokens = eBinCode.split(" ");
        for(int i = 0; i < eBinTokens.length; i++){
            switch(eBinTokens[i]){
                case "0000000000000001":
                    incrementPointerValue();
                    break;
                case "0000000000000010":
                    decrementPointerValue();
                    break;
                case "0000000000000011":
                    movePointerRight();
                    break;
                case "0000000000000100":
                    movePointerLeft();
                    break;
                case "0000000000000101":
                    if (Tape[pointerX][pointerY].isZero()) {
                        i++;
                        while(c > 0 || !eBinTokens[i].equals("0000000000000110")) {
                            if(eBinTokens[i].equals("0000000000000101")) {
                                c++;
                            } else if(eBinTokens[i].equals("0000000000000110")) {
                                c--;
                            } else if(eBinTokens[i].equals("0000000000001011")) {
                                throw new Exception("Looped Dependency Set Call" + tokenNumber);  
                            }
                            i++;
                        }
                    }
                    break;
                case "0000000000000110":
                    if (!Tape[pointerX][pointerY].isZero()) {
                        i--;
                        while(c > 0 || !eBinTokens[i].equals("0000000000000101")) {
                            
                            if(eBinTokens[i].equals("0000000000000110")) {
                                c++;
                            } else if(eBinTokens[i].equals("0000000000000101")) {
                                c--;
                            } else if(eBinTokens[i].equals("0000000000001011")) {
                                throw new Exception("Looped Dependency Set Call" + tokenNumber);  
                            }
                            i--;
                        }
                    }
                    break;
                case "0000000000000111":
                    Tape[pointerX][pointerY] = DoubleByte.convertToDoubleByte(pointerValue);
                    break;
                case "0000000000001101": // read from Tape
                    pointerValue = Tape[pointerX][pointerY].convertToInt();
                    break;
                case "0000000000001000":
                    Tape[pointerX][pointerY].setHighByte((byte) System.in.read());
                    Tape[pointerX][pointerY].setLowByte((byte) System.in.read());
                    break;
                case "0000000000001001":
                    incrementStackPointer();
                    stack[stackPointer] = DoubleByte.convertToDoubleByte(pointerValue);
                    pointerValue = 0;
                    break;
                case "0000000000001010":
                    pointerValue = stack[stackPointer].convertToInt();
                    stack[stackPointer] = DoubleByte.convertToDoubleByte(0);
                    decrementStackPointer();
                    break;
                case "0000000000001011":
                    if(eBinTokens[i+1].contains(".")){ dependencies.put(eBinTokens[i+2], new File(eBinTokens[i+1])); i += 2; }
                    break;
                case "0000000000001100":
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
                case "0000000000001110": // write to terminal
                    System.out.print((char) (Tape[pointerX][pointerY].convertToInt() + 32));
                    break;
                case "0000000000010001":
                    labels.put(eBinTokens[i+1], new int[]{ pointerX, pointerY });
                    i++;
                    break;
                case "0000000000010010":
                    if(labels.containsKey(eBinTokens[i+1])){
                        pointerX = labels.get(eBinTokens[i+1])[0];
                        pointerY = labels.get(eBinTokens[i+1])[1];
                    }
                    i++;
                    break;
                case "0000000000001111": // END
                    break;
                default:
                    throw new UnrecognizedTokenException("Unrecognized Token: " + eBinTokens[i] + " at token number: " + tokenNumber);
            }
            tokenNumber++;
            debugStats(DEBUG_FLAG, eBinTokens[i]);
        }
    }

    private static boolean DEBUG_FLAG = false;

    public static void main(String[] args){
        initializeTape();
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

    private static void initializeTape(){
        for(int i = 0; i < 255; i++){
            for(int j = 0; j < 255; j++){
                Tape[i][j] = new DoubleByte((byte) 0x0000, (byte) 0x0000);
            }
        }
    }

    private static void initializeStack(){
        for(int i = 0; i < stack.length; i++){
            stack[i] = new DoubleByte((byte) 0x0000, (byte) 0x0000);
        }
    }

    private static void debugStats(boolean flag, String token){
        if(flag){
            // Token
            System.out.println("Token: " + tokenNumber + " ( " + token + " )");

            // Tape
            for(int i = 0; i < Tape.length; i++){
                for(int j = 0; j < Tape[i].length; j++){
                    System.out.print("[" + Tape[i][j].convertToInt() + "]");
                }
            }
            System.out.println();
            
            // Tape pointer
            int totalPointerPosition = (pointerX * 256) + pointerY;
            for(int i = 1; i < totalPointerPosition; i++){
                System.out.print(" ");
            }
            System.out.println("*");

            // Stack
            System.out.print("[ ");
            for(int i = 0; i < stack.length; i++){
                System.out.print(stack[i].convertToInt() + " ");
            }
            System.out.println(" ]");

            // Pointer Value
            System.out.println("PV = " + pointerValue);

            // Labels
            System.out.print("Labels: { ");
            for(String key : labels.keySet()){
                int x = labels.get(key)[0];
                int y = labels.get(key)[1];
                int labelPosition = (x * 256) + y;
                System.out.print(key + "[" + labelPosition + "] ");
            }
            System.out.println("}");
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
}
