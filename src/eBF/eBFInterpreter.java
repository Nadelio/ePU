package eBF;

import java.io.File;
import java.util.Scanner;

import ePUx16Sim.UnrecognizedTokenException;
import ePUx16Sim.Word;
import ePUx16Sim.UnsignedByte;

import java.util.HashMap;

public class eBFInterpreter {

    // STACK OPERATIONS
    private static Word[] stack = new Word[256];
    private static int stackPointer = 0;

    // TAPE OPERATIONS
    private static int pointerX = 0;
    private static int pointerY = 0;
    private static int pointerValue = 0;
    private static Word[][] Tape = new Word[256][256];
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
                    Tape[pointerX][pointerY] = Word.convertToWord(pointerValue);
                    break;
                case "=": // write to terminal
                    System.out.print((char) (Tape[pointerX][pointerY].convertToInt()));
                    break;
                case ".":
                    Tape[pointerX][pointerY].setHighByte(new UnsignedByte((byte) System.in.read()));
                    Tape[pointerX][pointerY].setLowByte(new UnsignedByte((byte) System.in.read()));
                    break;
                case ">>":
                    incrementStackPointer();
                    stack[stackPointer] = Word.convertToWord(pointerValue);
                    pointerValue = 0;
                    break;
                case "<<":
                    pointerValue = stack[stackPointer].convertToInt();
                    stack[stackPointer] = Word.convertToWord(0);
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
                case "!#":
                    if(labels.containsKey(eBFTokens[i+1])){
                        labels.remove(eBFTokens[i+1]);    
                    }
                    i++;
                    break;
                case "\"":
                    pointerValue = (pointerY * 256) + pointerX;
                    break;
                case "/*":
                    i++;
                    while (i < eBFTokens.length && !eBFTokens[i].equals("*/")) { i++; }
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
                case "0000000000000001": // increment pointer value
                    incrementPointerValue();
                    break;
                case "0000000000000010": // decrement pointer value
                    decrementPointerValue();
                    break;
                case "0000000000000011": // move right
                    movePointerRight();
                    break;
                case "0000000000000100": // move left
                    movePointerLeft();
                    break;
                case "0000000000000101": // start loop
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
                case "0000000000000110": // end loop
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
                case "0000000000000111": // write to Tape
                    Tape[pointerX][pointerY] = Word.convertToWord(pointerValue);
                    break;
                case "0000000000001101": // read from Tape
                    pointerValue = Tape[pointerX][pointerY].convertToInt();
                    break;
                case "0000000000001000": // read from stdin
                    Tape[pointerX][pointerY].setHighByte(new UnsignedByte((byte) System.in.read()));
                    Tape[pointerX][pointerY].setLowByte(new UnsignedByte((byte) System.in.read()));
                    break;
                case "0000000000001001": // push
                    incrementStackPointer();
                    stack[stackPointer] = Word.convertToWord(pointerValue);
                    pointerValue = 0;
                    break;
                case "0000000000001010": // pop
                    pointerValue = stack[stackPointer].convertToInt();
                    stack[stackPointer] = Word.convertToWord(0);
                    decrementStackPointer();
                    break;
                case "0000000000001011": // set dependency
                    if(eBinTokens[i+1].contains(".")){ dependencies.put(eBinTokens[i+2], new File(eBinTokens[i+1])); i += 2; }
                    break;
                case "0000000000001100": // dependency call
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
                    System.out.print((char) (Tape[pointerX][pointerY].convertToInt()));
                    break;
                case "0000000000010000": // create label
                    labels.put(eBinTokens[i+1], new int[]{ pointerX, pointerY });
                    i++;
                    break;
                case "0000000000010001": // move pointer to label
                    if(labels.containsKey(eBinTokens[i+1])){
                        pointerX = labels.get(eBinTokens[i+1])[0];
                        pointerY = labels.get(eBinTokens[i+1])[1];
                    }
                    i++;
                    break;
                case "0000000000010010": // remove label
                    if(labels.containsKey(eBinTokens[i+1])){
                        labels.remove(eBinTokens[i+1]);    
                    }
                    i++;
                    break;
                case "0000000000010011": // set pointer value to pointer position
                    pointerValue = (pointerY * 256) + pointerX;
                    break;
                case "0000000000010100": // set pointer value
                    pointerValue = Integer.parseInt(eBinTokens[i+1] + eBinTokens[i+2], 2);
                    i += 2;
                    break;
                case "0000000000010111": // set pointer position
                    pointerX = Integer.parseInt(eBinTokens[i+1], 2);
                    pointerY = Integer.parseInt(eBinTokens[i+2], 2);
                    i += 2;
                    break;
                case "0000000000010101": // move up
                    pointerY++;
                    if(pointerY == 255){ pointerY = 0; }
                    break;
                case "0000000000010110": // move down
                    pointerY--;
                    if(pointerY == -1){ pointerY = 255; }
                    break;
                case "0000000000011000": // interrupts bytecode
                    //! both arguments are ALWAYS labels
                    String firstArgument = eBinTokens[i+1];
                    String comparison = eBinTokens[i+2];
                    String secondArgument = eBinTokens[i+3];
                    String dependency = eBinTokens[i+4];
                    int[] firstValPos = labels.get(firstArgument);
                    int[] secondValPos = labels.get(secondArgument);
                    switch(comparison){
                        case "0000000000000000": // not equal
                            if(Tape[firstValPos[0]][firstValPos[1]].convertToInt() != Tape[secondValPos[0]][secondValPos[1]].convertToInt()){
                                if(dependencies.get(dependency).getName().endsWith(".ebin")){
                                    interpretEBIN(dependencies.get(dependency));
                                } else if(dependencies.get(dependency).getName().endsWith(".ebf")){
                                    interpretEBF(dependencies.get(dependency));
                                }
                            }
                            break;
                        case "0000000000000001": // equal
                            if(Tape[firstValPos[0]][firstValPos[1]].convertToInt() == Tape[secondValPos[0]][secondValPos[1]].convertToInt()){
                                if(dependencies.get(dependency).getName().endsWith(".ebin")){
                                    interpretEBIN(dependencies.get(dependency));
                                } else if(dependencies.get(dependency).getName().endsWith(".ebf")){
                                    interpretEBF(dependencies.get(dependency));
                                }
                            }
                            break;
                        case "0000000000000010": // greater than
                            if(Tape[firstValPos[0]][firstValPos[1]].convertToInt() > Tape[secondValPos[0]][secondValPos[1]].convertToInt()){
                                if(dependencies.get(dependency).getName().endsWith(".ebin")){
                                    interpretEBIN(dependencies.get(dependency));
                                } else if(dependencies.get(dependency).getName().endsWith(".ebf")){
                                    interpretEBF(dependencies.get(dependency));
                                }
                            }
                            break;
                        case "0000000000000011": // less than
                            if(Tape[firstValPos[0]][firstValPos[1]].convertToInt() < Tape[secondValPos[0]][secondValPos[1]].convertToInt()){
                                if(dependencies.get(dependency).getName().endsWith(".ebin")){
                                    interpretEBIN(dependencies.get(dependency));
                                } else if(dependencies.get(dependency).getName().endsWith(".ebf")){
                                    interpretEBF(dependencies.get(dependency));
                                }
                            }
                            break;
                        case "0000000000000100": // greater than or equal to
                            if(Tape[firstValPos[0]][firstValPos[1]].convertToInt() >= Tape[secondValPos[0]][secondValPos[1]].convertToInt()){
                                if(dependencies.get(dependency).getName().endsWith(".ebin")){
                                    interpretEBIN(dependencies.get(dependency));
                                } else if(dependencies.get(dependency).getName().endsWith(".ebf")){
                                    interpretEBF(dependencies.get(dependency));
                                }
                            }
                            break;
                        case "0000000000000101": // less than or equal to
                            if(Tape[firstValPos[0]][firstValPos[1]].convertToInt() <= Tape[secondValPos[0]][secondValPos[1]].convertToInt()){
                                if(dependencies.get(dependency).getName().endsWith(".ebin")){
                                    interpretEBIN(dependencies.get(dependency));
                                } else if(dependencies.get(dependency).getName().endsWith(".ebf")){
                                    interpretEBF(dependencies.get(dependency));
                                }
                            }
                            break;
                        default:
                            throw new UnrecognizedTokenException("Unrecognized Token: " + comparison + " at token number: " + (tokenNumber + 2));
                    }
                    i += 4;
                    break;    
                case "0000000000011001": // NOP
                    Thread.sleep(10);
                    break;
                case "0000000000011100": // Write hardcode value to tape
                    UnsignedByte highByte = new UnsignedByte((byte) Integer.parseInt(eBinTokens[i+1], 2));
                    UnsignedByte lowByte = new UnsignedByte((byte) Integer.parseInt(eBinTokens[i+2], 2));
                    Tape[pointerX][pointerY] = new Word(highByte, lowByte);
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
        for(int i = 0; i < 256; i++){
            for(int j = 0; j < 256; j++){
                Tape[i][j] = Word.zero();
            }
        }
    }

    private static void initializeStack(){
        for(int i = 0; i < stack.length; i++){
            stack[i] = Word.zero();
        }
    }

    private static void debugStats(boolean flag, String token){
        if(flag){
            // Token
            System.out.println("Token: " + tokenNumber + " ( " + token + " )");

            // Tape
            if(pointerX == 0) {
                System.out.print("{" + Tape[pointerX][pointerY].convertToInt() + "}");
                System.out.print("[" + Tape[pointerX + 1][pointerY].convertToInt() + "]");
                System.out.print("[" + Tape[pointerX + 2][pointerY].convertToInt() + "]");
                System.out.print("[" + Tape[pointerX + 3][pointerY].convertToInt() + "]");
                System.out.println("[" + Tape[pointerX + 4][pointerY].convertToInt() + "]");
            } else if(pointerX == 1) {
                System.out.print("[" + Tape[pointerX - 1][pointerY].convertToInt() + "]");
                System.out.print("{" + Tape[pointerX][pointerY].convertToInt() + "}");
                System.out.print("[" + Tape[pointerX + 1][pointerY].convertToInt() + "]");
                System.out.print("[" + Tape[pointerX + 2][pointerY].convertToInt() + "]");
                System.out.println("[" + Tape[pointerX + 3][pointerY].convertToInt() + "]");
            } else if(pointerX == 255) {
                System.out.print("[" + Tape[pointerX - 4][pointerY].convertToInt() + "]");
                System.out.print("[" + Tape[pointerX - 3][pointerY].convertToInt() + "]");
                System.out.print("[" + Tape[pointerX - 2][pointerY].convertToInt() + "]");
                System.out.print("[" + Tape[pointerX - 1][pointerY].convertToInt() + "]");
                System.out.println("{" + Tape[pointerX][pointerY].convertToInt() + "}");
            } else if(pointerX == 254) { 
                System.out.print("[" + Tape[pointerX - 1][pointerY].convertToInt() + "]");
                System.out.print("[" + Tape[pointerX - 2][pointerY].convertToInt() + "]");
                System.out.print("[" + Tape[pointerX - 3][pointerY].convertToInt() + "]");
                System.out.print("{" + Tape[pointerX][pointerY].convertToInt() + "}");
                System.out.println("[" + Tape[pointerX + 1][pointerY].convertToInt() + "]");
            } else if(pointerX >= 2 && pointerX <= 253){
                System.out.print("[" + Tape[pointerX - 2][pointerY].convertToInt() + "]");
                System.out.print("[" + Tape[pointerX - 1][pointerY].convertToInt() + "]");
                System.out.print("{" + Tape[pointerX][pointerY].convertToInt() + "}");
                System.out.print("[" + Tape[pointerX + 1][pointerY].convertToInt() + "]");
                System.out.println("[" + Tape[pointerX + 2][pointerY].convertToInt() + "]");
            }

            // Stack
            System.out.print("[ ");
            for(int i = 0; i < stack.length; i++){
                if(stack[i].convertToInt() != 0){
                    System.out.print(stack[i].convertToInt() + " ");
                }
            }
            System.out.println(" ]");

            // Pointer Value
            System.out.println("PV = " + pointerValue);

            // Labels
            System.out.print("Labels: { ");
            for(String key : labels.keySet()){
                int x = labels.get(key)[0];
                int y = labels.get(key)[1];
                int labelPosition = (y * 256) + x;
                System.out.print(key + "[" + labelPosition + "]: " + Tape[x][y].convertToInt() + " ");
            }
            System.out.println("}");
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
}
