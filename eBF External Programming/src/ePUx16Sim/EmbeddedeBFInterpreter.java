package ePUx16Sim;

import java.io.File;
import java.util.Scanner;
import java.util.HashMap;

public class EmbeddedeBFInterpreter {

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
    private static HashMap<UnsignedByte, UnsignedByte[]> dependencies = new HashMap<UnsignedByte, UnsignedByte[]>();
    private static HashMap<UnsignedByte, Word> dependencySizes = new HashMap<UnsignedByte, Word>();
    private static HashMap<UnsignedByte, int[]> labels = new HashMap<UnsignedByte, int[]>();

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
                    RAMUnit.RAM[pointerX][pointerY].setHighByte(new UnsignedByte((byte) System.in.read()));
                    RAMUnit.RAM[pointerX][pointerY].setLowByte(new UnsignedByte((byte) System.in.read()));
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
                    dependencies.put(eBinTokens[i+3], new UnsignedByte[]{ eBinTokens[i+1], eBinTokens[i+2] }); // ( Alias, { X, Y } )
                    Word size = Word.convertToWord(Registers.findSize(dependencies.get(eBinTokens[i+3])[0], dependencies.get(eBinTokens[i+3])[1])); // ( Alias.x, Alias.y )
                    Registers.readInProgram(dependencies.get(eBinTokens[i+3])[0], dependencies.get(eBinTokens[i+3])[1], size); // ( Alias.x, Alias.y, Size )
                    dependencySizes.put(eBinTokens[i+1], size);
                    i += 3;
                    break;
                case 13: // dependency call
                    if(dependencies.containsKey(eBinTokens[i+1])){
                        Registers.startProgram(dependencies.get(eBinTokens[i+1])[0], dependencies.get(eBinTokens[i+1])[1], dependencySizes.get(eBinTokens[i+1]));
                    }
                    break;
                case 14: // sys call
                    if(eBinTokens[i+1].value == 26){ // '{'
                        ControlUnit.commandUnit(getLabelValues(eBinTokens, i));
                        if(eBinTokens[i+7].value != 27){ // '}'
                            throw new Exception("Unmatched '{' at token number: " + (tokenNumber + 7));
                        }
                        i += 7;
                    } else {
                        ControlUnit.commandUnit(new UnsignedByte[]{ eBinTokens[i+1], eBinTokens[i+2], eBinTokens[i+3], eBinTokens[i+4], eBinTokens[i+5] });
                        i += 5;
                    }
                    break;
                case 15: // END
                    break;
                case 16: // build label
                    labels.put(eBinTokens[i+1], new int[]{ pointerX, pointerY });
                    i++;
                    break;
                case 17: // jump to label
                    if(labels.containsKey(eBinTokens[i+1])){
                        pointerX = labels.get(eBinTokens[i+1])[0];
                        pointerY = labels.get(eBinTokens[i+1])[1];
                    }
                    i++;
                    break;
                case 18: // delete label
                    if(labels.containsKey(eBinTokens[i+1])){
                        labels.remove(eBinTokens[i+1]);    
                    }
                    i++;
                    break;
                case 19: // read cell position
                    pointerValue = (pointerY * 256) + pointerX;
                    break;
                case 20: // set pointer value
                    setPointerValue(new Word(eBinTokens[i+1], eBinTokens[i+2]));
                    i += 2;
                    break;
                case 21: // move pointer up
                    pointerY--;
                    if(pointerY == -1){ pointerY = 255; }
                    break;
                case 22: // move pointer down
                    pointerY++;
                    if(pointerY == 255){ pointerY = 0; }
                    break;
                case 23: // set pointer position
                    pointerX = eBinTokens[i+1].value;
                    pointerY = eBinTokens[i+2].value;
                    i += 2;
                    break;
                case 24: // interrupts
                    //! both arguments are ALWAYS labels
                    UnsignedByte firstArgument = eBinTokens[i+1];
                    UnsignedByte comparison = eBinTokens[i+2];
                    UnsignedByte secondArgument = eBinTokens[i+3];
                    UnsignedByte dependency = eBinTokens[i+4];
                    int[] firstValPos = labels.get(firstArgument);
                    int[] secondValPos = labels.get(secondArgument);
                    switch(comparison.value){
                        case 0: // not equal
                            if(RAMUnit.RAM[firstValPos[0]][firstValPos[1]].convertToInt() != RAMUnit.RAM[secondValPos[0]][secondValPos[1]].convertToInt()){
                                if(dependencies.containsKey(dependency)){
                                    Registers.startProgram(dependencies.get(dependency)[0], dependencies.get(dependency)[1], dependencySizes.get(dependency));
                                }
                            }
                            break;
                        case 1: // equal
                            if(RAMUnit.RAM[firstValPos[0]][firstValPos[1]].convertToInt() == RAMUnit.RAM[secondValPos[0]][secondValPos[1]].convertToInt()){
                                if(dependencies.containsKey(dependency)){
                                    Registers.startProgram(dependencies.get(dependency)[0], dependencies.get(dependency)[1], dependencySizes.get(dependency));
                                }
                            }
                            break;
                        case 2: // greater than
                            if(RAMUnit.RAM[firstValPos[0]][firstValPos[1]].convertToInt() > RAMUnit.RAM[secondValPos[0]][secondValPos[1]].convertToInt()){
                                if(dependencies.containsKey(dependency)){
                                    Registers.startProgram(dependencies.get(dependency)[0], dependencies.get(dependency)[1], dependencySizes.get(dependency));
                                }
                            }
                            break;
                        case 3: // less than
                            if(RAMUnit.RAM[firstValPos[0]][firstValPos[1]].convertToInt() < RAMUnit.RAM[secondValPos[0]][secondValPos[1]].convertToInt()){
                                if(dependencies.containsKey(dependency)){
                                    Registers.startProgram(dependencies.get(dependency)[0], dependencies.get(dependency)[1], dependencySizes.get(dependency));
                                }
                            }
                            break;
                        case 4: // greater than or equal to
                            if(RAMUnit.RAM[firstValPos[0]][firstValPos[1]].convertToInt() >= RAMUnit.RAM[secondValPos[0]][secondValPos[1]].convertToInt()){
                                if(dependencies.containsKey(dependency)){
                                    Registers.startProgram(dependencies.get(dependency)[0], dependencies.get(dependency)[1], dependencySizes.get(dependency));
                                }
                            }
                            break;
                        case 5: // less than or equal to
                            if(RAMUnit.RAM[firstValPos[0]][firstValPos[1]].convertToInt() <= RAMUnit.RAM[secondValPos[0]][secondValPos[1]].convertToInt()){
                                if(dependencies.containsKey(dependency)){
                                    Registers.startProgram(dependencies.get(dependency)[0], dependencies.get(dependency)[1], dependencySizes.get(dependency));
                                }
                            }
                            break;
                        default:
                            throw new UnrecognizedTokenException("Unrecognized Token: " + comparison + " at token number: " + (tokenNumber + 2));
                    }
                    i += 4;
                    break;
                case 25: // NOP
                    Thread.sleep(10);
                    break;  
                default:
                    throw new UnrecognizedTokenException("Unrecognized Token: " + eBinStrings[i] + " at token number: " + tokenNumber);
            }
            tokenNumber++;
        }
    }

    public static UnsignedByte[] toUnsignedByte(String[] s){
        byte[] b = new byte[s.length];
        for(int i = 0; i < s.length; i++){
            if (!s[i].isEmpty()) {
                try{
                    b[i] = (byte) Integer.parseInt(s[i], 2);
                } catch (NumberFormatException e) {
                    b[i] = (byte) -1;
                }
            } else {
                throw new NumberFormatException("Empty string at index " + i);
            }
        }

        UnsignedByte[] ub = new UnsignedByte[b.length];
        for(int i = 0; i < b.length; i++){
            if(b[i] == -1){
                ub[i] = new UnsignedByte(255);
            } else {
                ub[i] = new UnsignedByte(b[i]);
            }
        }
        return ub;
    }

    public static void initializeStack(){
        for(int i = 0; i < stack.length; i++){
            stack[i] = new Word(UnsignedByte.zero(), UnsignedByte.zero());
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

    private static UnsignedByte[] getLabelValues(UnsignedByte[] eBinTokens, int i){
        int[][] labelPositions = new int[5][2];
        labelPositions[0] = labels.get(eBinTokens[i+2]);
        labelPositions[1] = labels.get(eBinTokens[i+3]);
        labelPositions[2] = labels.get(eBinTokens[i+4]);
        labelPositions[3] = labels.get(eBinTokens[i+5]);
        labelPositions[4] = labels.get(eBinTokens[i+6]);

        UnsignedByte[] labelValues = new UnsignedByte[5];
        for(int j = 0; j < labelPositions.length; j++){
            labelValues[j] = new UnsignedByte(RAMUnit.RAM[labelPositions[j][0]][labelPositions[j][1]].convertToInt());
        }

        return labelValues;
    }

    public static void setPointerValue(Word value){ pointerValue = value.convertToInt(); }
    public static int getPointerValue(){ return pointerValue; }
}
