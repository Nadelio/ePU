package eBF;
import java.io.File;
import java.util.Scanner;

import ePUx16Sim.ControlUnit;
import ePUx16Sim.RAMUnit;
import ePUx16Sim.Registers;
import ePUx16Sim.UnsignedByte;

import java.util.HashMap;

public class eBFInterpreter {

    // STACK OPERATIONS
    private static Word[] stack = new Word[256];
    private static int stackPointer = 0;

    // RAM OPERATIONS
    private static int pointerX = 0;
    private static int pointerY = 0;
    private static int pointerValue = 0;
    private static final int maxPointerValue = (int) Math.pow(2, 8) - 1;
    
    // MISC VARS
    private static int tokenNumber = 0;
    private static HashMap<String, UnsignedByte[]> dependencies = new HashMap<String, UnsignedByte[]>();
    private static HashMap<String, Word> dependencySizes = new HashMap<String, Word>();

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
                    RAMUnit.RAM[pointerX][pointerY] = Word.convertToWord(pointerValue);
                    break;
                case "=": // write to terminal
                    System.out.print((char) (RAMUnit.RAM[pointerX][pointerY].convertToInt() + 32));
                    break;
                case ".":
                    RAMUnit.RAM[pointerX][pointerY].setHighByte(new UnsignedByte(System.in.read()));
                    RAMUnit.RAM[pointerX][pointerY].setLowByte(new UnsignedByte(System.in.read()));
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
                    // if(eBFTokens[i+1].contains(".")){ dependencies.put(eBFTokens[i+2], new File(eBFTokens[i+1])); i += 2; }
                    break;
                case "%":
                    // if(dependencies.containsKey(eBFTokens[i+1])){
                    //     if(dependencies.get(eBFTokens[i+1]).getName().endsWith(".ebin")){
                    //         interpretEBIN(dependencies.get(eBFTokens[i+1]));
                    //         i++;
                    //     } else if(dependencies.get(eBFTokens[i+1]).getName().endsWith(".ebf")){
                    //         interpretEBF(dependencies.get(eBFTokens[i+1]));
                    //         i++;
                    //     } else {
                    //         System.out.println("Error: Invalid Dependency File Type");
                    //     }
                    // }
                    break;
                case "'": // read from RAM
                    pointerValue = RAMUnit.RAM[pointerX][pointerY].convertToInt();
                    break;
                // case "$": // sys call
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

    public static void interpretEBIN(File eBinaryFile) throws Exception{
        Scanner sc = new Scanner(eBinaryFile);
        String eBinCode = "";
        while(sc.hasNextLine()){ eBinCode += sc.nextLine(); }
        sc.close();

        int c = 0;

        String[] eBinStrings = eBinCode.split(" ");
        UnsignedByte[] eBinTokens = toUnsignedByte(eBinStrings);
        for(int i = 0; i < eBinTokens.length; i++){
            switch(eBinTokens[i].value){ // interpret eBin tokens
                case 1: // increment pointer value
                    incrementPointerValue();
                    break;
                case 2: // decrement pointer value
                    decrementPointerValue();
                    break;
                case 3: // move pointer right
                    movePointerRight();
                    break;
                case 4: // move pointer left
                    movePointerLeft();
                    break;
                case 5: // [
                    if (RAMUnit.RAM[pointerX][pointerY].isZero()) {
                        i++;
                        while(c > 0 || !(eBinTokens[i].value == 6)) {
                            if(eBinTokens[i].value == 5) {
                                c++;
                            } else if(eBinTokens[i].value == 6) {
                                c--;
                            } else if(eBinTokens[i].value == 12) {
                                throw new Exception("Looped Dependency Set Call" + tokenNumber);  
                            }
                            i++;
                        }
                    }
                    break;
                case 6: // ]
                    if (!RAMUnit.RAM[pointerX][pointerY].isZero()) {
                        i--;
                        while(c > 0 || !(eBinTokens[i].value == 5)) {
                            
                            if(eBinTokens[i].value == 6) {
                                c++;
                            } else if(eBinTokens[i].value == 5) {
                                c--;
                            } else if(eBinTokens[i].value == 12) {
                                throw new Exception("Looped Dependency Set Call" + tokenNumber);  
                            }
                            i--;
                        }
                    }
                    break;
                case 7: // write to RAM
                    RAMUnit.RAM[pointerX][pointerY] = Word.convertToWord(pointerValue);
                    break;
                case 8: // read from RAM
                    pointerValue = RAMUnit.RAM[pointerX][pointerY].convertToInt();
                    break;
                case 9: // read from user
                    System.out.print("Enter a character: ");
                    RAMUnit.RAM[pointerX][pointerY].setHighByte(new UnsignedByte(System.in.read()));
                    System.out.print("\nEnter another character: ");
                    RAMUnit.RAM[pointerX][pointerY].setLowByte(new UnsignedByte(System.in.read()));
                    System.out.println();
                    break;
                case 10: // push
                    incrementStackPointer();
                    stack[stackPointer] = Word.convertToWord(pointerValue);
                    pointerValue = 0;
                    break;
                case 11: // pop
                    pointerValue = stack[stackPointer].convertToInt();
                    stack[stackPointer] = Word.convertToWord(0);
                    decrementStackPointer();
                    break;
                case 12: // dependency load
                    dependencies.put(eBinStrings[i+3], new UnsignedByte[]{ eBinTokens[i+1], eBinTokens[i+2] }); // ( Alias, { X, Y } )
                    Word size = Word.convertToWord(Registers.findSize(dependencies.get(eBinStrings[i+3])[0], dependencies.get(eBinStrings[i+3])[1])); // ( Alias.x, Alias.y )
                    Registers.readInProgram(dependencies.get(eBinStrings[i+3])[0], dependencies.get(eBinStrings[i+3])[1], size); // ( Alias.x, Alias.y, Size )
                    dependencySizes.put(eBinStrings[i+1], size);
                    i += 3;
                    break;
                case 13: // dependency call
                    if(dependencies.containsKey(eBinStrings[i+1])){
                        Registers.startProgram(dependencies.get(eBinStrings[i+1])[0], dependencies.get(eBinStrings[i+1])[1], dependencySizes.get(eBinStrings[i+1]));
                    }
                    break;
                case 14: // sys call
                    ControlUnit.commandUnit(new UnsignedByte[]{ eBinTokens[i+1], eBinTokens[i+2], eBinTokens[i+3], eBinTokens[i+4], eBinTokens[i+5] });
                    i += 5;
                    break;
                case 15: // END
                    break;
                default:
                    throw new UnrecognizedTokenException("Unrecognized Token: " + eBinTokens[i] + " at token number: " + tokenNumber);
            }
            tokenNumber++;
            debugStats(DEBUG_FLAG, eBinStrings[i]);
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

    public static UnsignedByte[] toUnsignedByte(String[] s){
        byte[] b = new byte[s.length];
        for(int i = 0; i < s.length; i++){
            if (!s[i].isEmpty()) {
                b[i] = (byte) Integer.parseInt(s[i], 2);
            } else {
                throw new NumberFormatException("Empty string at index " + i);
            }
        }

        UnsignedByte[] ub = new UnsignedByte[b.length];
        for(int i = 0; i < b.length; i++){
            ub[i] = new UnsignedByte(b[i]);
        }
        return ub;
    }

    @Deprecated
    private static void initializeRAM(){
        for(int i = 0; i < 255; i++){
            for(int j = 0; j < 255; j++){
                RAMUnit.RAM[i][j] = new Word(UnsignedByte.zero(), UnsignedByte.zero());
            }
        }
    }

    public static void initializeStack(){
        for(int i = 0; i < stack.length; i++){
            stack[i] = new Word(UnsignedByte.zero(), UnsignedByte.zero());
        }
    }

    @Deprecated
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

    public static void setPointerValue(Word value){ pointerValue = value.convertToInt(); }
    public static int getPointerValue(){ return pointerValue; }
}
