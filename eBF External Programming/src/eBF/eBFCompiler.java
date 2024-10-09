package eBF;

import java.io.File;
import java.util.HashMap;

import ePUx16Sim.UnrecognizedTokenException;
import ePUx16Sim.Word;

public class eBFCompiler{
    // this compiles eBF tokens into eBin

    private static final String[] eBFTokens = {"!E", "+", "-", ">", "<", "[", "]", ",", ".", ">>", "<<", "DPND", "%", "$", "=", "'", "#", "!#", "@", "\"", "END"};
    private static final String[] eBinTokens = {"0000000000000001", "0000000000000010", "0000000000000011", "0000000000000100", "0000000000000101", "0000000000000110", "0000000000000111", "0000000000001000", "0000000000001001", "0000000000001010", "0000000000001011", "0000000000001100", "0000000000001101", "0000000000001110", "0000000000001111", "0000000000010000", "0000000000010001", "0000000000010010", "0000000000010011"};
                                              // 0                   1                   2                   3                   4                   5                   6                   7                   8                   9                   10                  11                  12                  13                  14                  15                  16                  17                  18
    private static final String[] tokenNames = {"{INCREMENT}", "{DECREMENT}", "{POINTER_INCREMENT}", "{POINTER_DECREMENT}", "{LOOP_START}", "{LOOP_END}", "{WRITE_TO_RAM}", "{INPUT}", "{PUSH_STACK}", "{POP_STACK}", "{DEPENDENCY}", "{DEPENDENCY_VALUE}", "{DEPENDENCY_X_ADDRESS}", "{DEPENDENCY_Y_ADDRESS}", "{DEPENDENCY_ALIAS}", "{DEPENDENCY_CALL}", "{SYSTEM_CALL}", "{SYSTEM_CALL_VALUE}", "{END_PROGRAM}", "{WRITE_TO_TERMINAL}", "{READ_FROM_RAM}", "{CREATE_LABEL}", "{JUMP_TO_LABEL}", "{LABEL_ALIAS}", "{REMOVE_LABEL}", "{READ_CELL_POSITION}"};
                                              // 0,             1,             2,                     3,                     4,              5,            6,                7,         8,              9,             10,             11,                   12,                       13,                       14,                   15,                  16,              17,                    18,              19,                    20                 21                22                 23               24                25
    private static String eBFtoString = "";
    private static String processedeBFCode = "";

    private static String compile(String eBFCode) throws UnrecognizedTokenException{
        String eBinCode = "";
        
        boolean EMBEDDED_eBF_FLAG = false;
        int labelIndex = 0;
        int dependencyIndex = 0;

        HashMap<String, Integer> labelIndexMap = new java.util.HashMap<String, Integer>();
        HashMap<String, Integer> dependencyIndexMap = new java.util.HashMap<String, Integer>();

        // split eBF code into tokens
        String[] tokens = eBFCode.split(" ");
        for(int j = 0; j < tokens.length; j++){
            for(int i = 0; i < eBFTokens.length; i++){
                if(tokens[j].equals(eBFTokens[i])){

                    // output current and next token for debugging purposes
                    if(DEBUG_FLAG){
                        System.out.println("Current Token: " + tokens[j]);
                        try{
                            System.out.println("Next Token: " + tokens[j+1] + "\n");
                        } catch(ArrayIndexOutOfBoundsException e){
                            System.out.println("Next Token: N/A\n");
                        }
                    }
                    
                    // compile eBF tokens into eBin
                    switch(tokens[j]){
                        case "!E":
                            EMBEDDED_eBF_FLAG = true;
                            break;
                        case "+":
                            eBinCode += eBinTokens[0]  + " ";
                            eBFtoString += tokenNames[0] + " ";
                            processedeBFCode += tokens[j] + " ";
                            break;
                        case "-":
                            eBinCode += eBinTokens[1]  + " ";
                            eBFtoString += tokenNames[1] + " ";
                            processedeBFCode += tokens[j] + " ";
                            break;
                        case ">":
                            eBinCode += eBinTokens[2]  + " ";
                            eBFtoString += tokenNames[2] + " ";
                            processedeBFCode += tokens[j] + " ";
                            break;
                        case "<":
                            eBinCode += eBinTokens[3]  + " ";
                            eBFtoString += tokenNames[3] + " ";
                            processedeBFCode += tokens[j] + " ";
                            break;
                        case "[":
                            eBinCode += eBinTokens[4]  + " ";
                            eBFtoString += tokenNames[4] + " ";
                            processedeBFCode += tokens[j] + " ";
                            break;
                        case "]":
                            eBinCode += eBinTokens[5]  + " ";
                            eBFtoString += tokenNames[5] + " ";
                            processedeBFCode += tokens[j] + " ";
                            break;
                        case ",":
                            eBinCode += eBinTokens[6]  + " ";
                            eBFtoString += tokenNames[6] + " ";
                            processedeBFCode += tokens[j] + " ";
                            break;
                        case "'":
                            eBinCode += eBinTokens[12] + " ";
                            eBFtoString += tokenNames[20] + " ";
                            processedeBFCode += tokens[j] + " ";
                            break;
                        case ".":
                            eBinCode += eBinTokens[7]  + " ";
                            eBFtoString += tokenNames[7] + " ";
                            processedeBFCode += tokens[j] + " ";
                            break;
                        case ">>":
                            eBinCode += eBinTokens[8]  + " ";
                            eBFtoString += tokenNames[8] + " ";
                            processedeBFCode += tokens[j] + " ";
                            break;
                        case "<<":
                            eBinCode += eBinTokens[9]  + " ";
                            eBFtoString += tokenNames[9] + " ";
                            processedeBFCode += tokens[j] + " ";
                            break;
                        case "DPND":
                            if(tokens[j+1].contains(".")){
                                eBinCode += eBinTokens[10] + " " + tokens[j+1] + " " + tokens[j+2] + " ";
                                eBFtoString += tokenNames[10] + " " + tokenNames[11] + " " + tokenNames[14] + " ";
                                processedeBFCode += tokens[j] + " " + tokens[j+1] + " " + tokens[j+2] + " ";
                                j += 2;
                            } else {
                                eBinCode += eBinTokens[10] + " " + tokens[j+1] + " " + tokens[j+2] + " " + Word.convertToWord(dependencyIndex).toString() + " ";
                                dependencyIndexMap.put(tokens[j+3], dependencyIndex);
                                dependencyIndex++;
                                eBFtoString += tokenNames[10] + " " + tokenNames[12] + " " + tokenNames[13] + " " + tokenNames[14] + " ";
                                processedeBFCode += tokens[j] + " " + tokens[j+1] + " " + tokens[j+2] + " " + tokens[j+3] + " ";
                                j += 3;
                            }
                            break;
                        case "%":
                            if(EMBEDDED_eBF_FLAG){ eBinCode += eBinTokens[11] + " " + dependencyIndexMap.get(tokens[j+1]) + " "; }
                            else { eBinCode += eBinTokens[11] + " " + tokens[j+1] + " "; }
                            eBFtoString += tokenNames[15] + " " + tokenNames[14] + " ";
                            processedeBFCode += tokens[j] + " " + tokens[j+1] + " ";
                            j++;
                            break;
                        case "$":
                            eBinCode += eBinTokens[13] + " " + tokens[j+1] + " " + tokens[j+2] + " " + tokens[j+3] + " " + tokens[j+4] + " " + tokens[j+5] + " ";
                            eBFtoString += tokenNames[16] + " " + tokenNames[17] + " ";
                            processedeBFCode += tokens[j] + " " + tokens[j+1] + " " + tokens[j+2] + " " + tokens[j+3] + " " + tokens[j+4] + " " + tokens[j+5] + " ";
                            j += 4;
                            break;
                        case "#":
                            if(EMBEDDED_eBF_FLAG){
                                eBinCode += eBinTokens[15] + " " + Word.convertToWord(labelIndex).toString() + " ";
                                labelIndexMap.put(tokens[j+3], labelIndex);
                                labelIndex++;
                            }
                            else { eBinCode += eBinTokens[15] + " " + tokens[j+1] + " "; }
                            eBFtoString += tokenNames[21] + " " + tokenNames[23] + " ";
                            processedeBFCode += tokens[j] + " " + tokens[j+1] + " ";
                            j++;
                            break;
                        case "!#":
                            if(EMBEDDED_eBF_FLAG){ eBinCode += eBinTokens[17] + " " + labelIndexMap.get(tokens[j+1]) + " "; }
                            else { eBinCode += eBinTokens[17] + " " + tokens[j+1] + " "; }
                            eBFtoString += tokenNames[24] + " " + tokenNames[23] + " ";
                            processedeBFCode += tokens[j] + " " + tokens[j+1] + " ";
                            j++;
                            break;
                        case "@":
                            if(EMBEDDED_eBF_FLAG){ eBinCode += eBinTokens[16] + " " + labelIndexMap.get(tokens[j+1]) + " "; }
                            else { eBinCode += eBinTokens[16] + " " + tokens[j+1] + " "; }
                            eBFtoString += tokenNames[22] + " " + tokenNames[23] + " ";
                            processedeBFCode += tokens[j] + " " + tokens[j+1] + " ";
                            j++;
                            break;
                        case "=":
                            eBinCode += eBinTokens[13] + " ";
                            eBFtoString += tokenNames[19] + " ";
                            processedeBFCode += tokens[j] + " ";
                            break;
                        case "\"":
                            eBinCode += eBinTokens[18] + " ";
                            eBFtoString += tokenNames[25] + " ";
                            processedeBFCode += tokens[j] + " ";
                            break;
                        case "END":
                            eBinCode += eBinTokens[14];
                            eBFtoString += tokenNames[18];
                            processedeBFCode += tokens[j];
                            break;
                        default:
                            throw new UnrecognizedTokenException("Unrecognized token: " + tokens[j] + " at index " + j);
                    }
                    break;
                }
            }
        }
        return eBinCode;
    }

    public static boolean DEBUG_FLAG = false;
    public static boolean WRITE_FLAG = false;

    public static void main(String[] args) throws java.io.IOException{

        String eBFCode = "";

        // get eBF code from user
        if(args.length == 2){
            if(args[0].contains("-f")){
                try{
                    java.io.File file = new java.io.File(args[1]);
                    java.util.Scanner sc = new java.util.Scanner(file);
                    while(sc.hasNextLine()){
                        eBFCode += sc.nextLine() + " ";
                    }
                    sc.close();
                } catch(java.io.FileNotFoundException e) {
                    System.out.println("Given file not found.");
                    System.exit(1);
                }
            }

            if(args[0].contains("-d")){
                DEBUG_FLAG = true;
            }

            if(args[0].contains("-o")){
                WRITE_FLAG = true;
            }

        } else if(args.length == 1){
            System.out.println("Missing an argument.");
            System.exit(1);
        } else if(args.length == 0) {
            java.util.Scanner sc = new java.util.Scanner(System.in);
            System.out.print("CODE: ");
            eBFCode = sc.nextLine();
            System.out.println("");
            sc.close();
        } else {
            System.out.println("Too many arguments.");
            System.exit(1);
        }
        
        try {
            // compile eBF code
            String eBinCode;
            eBinCode = compile(eBFCode);

            // write eBin code to file
            if(WRITE_FLAG){
                File inputFile = new File(args[1]);
                File outputFile = new File(inputFile.getName().substring(0, inputFile.getName().length() - 4) + ".ebin");
                System.out.println("Writing eBin code to " + outputFile.getName());
                java.io.FileWriter fw = new java.io.FileWriter(outputFile);
                fw.write(eBinCode);
                fw.close();
                System.out.println("Completed writting eBin code to " + outputFile.getName());
            }

            // output various stages of compilation
            if(DEBUG_FLAG){
                System.out.println("eBF: " + eBFCode);
                System.out.println("eBin: " + eBinCode);
                System.out.println("Processed eBF: " + processedeBFCode);
                System.out.println("eBF Tokens: " + eBFtoString);
            }

        } catch(UnrecognizedTokenException e) { e.printStackTrace(); }
    }
}