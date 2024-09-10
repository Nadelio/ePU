public class eBFCompiler{
    // this compiles eBF tokens into eBin

    private static final String[] eBFTokens = {"+", "-", ">", "<", "[", "]", ",", ".", ">>", "<<", "DPND", "%", "$", "END"};
    private static final String[] eBinTokens = {"0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110"};
        private static final String[] tokenNames = {"{INCREMENT}", "{DECREMENT}", "{POINTER_INCREMENT}", "{POINTER_DECREMENT}", "{LOOP_START}", "{LOOP_END}", "{WRITE_TO_RAM}", "{INPUT}", "{PUSH_STACK}", "{POP_STACK}", "{DEPENDENCY}", "{DEPENDENCY_X_ADDRESS}", "{DEPENDENCY_Y_ADDRESS}", "{DEPENDENCY_ALIAS}", "{DEPENDENCY_CALL}", "{SYSTEM_CALL}", "{SYSTEM_CALL_VALUE}", "{END_PROGRAM}"};

    private static String eBFtoString = "";
    private static String processedeBFCode = "";

    private static String compile(String eBFCode){
        String eBinCode = "";
        
        // split eBF code into tokens
        String[] tokens = eBFCode.split(" ");

        for(int j = 0; j < tokens.length; j++){
            for(int i = 0; i < eBFTokens.length; i++){
                if(tokens[j].equals(eBFTokens[i])){

                    // output current and next token for debugging purposes
                    System.out.println("Current Token: " + tokens[j]);
                    try{
                        System.out.println("Next Token: " + tokens[j+1] + "\n");
                    } catch(ArrayIndexOutOfBoundsException e){
                        System.out.println("Next Token: N/A\n");
                    }
                    
                    // compile eBF tokens into eBin
                    switch(tokens[j]){
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
                            eBinCode += eBinTokens[10] + " " + tokens[j+1] + " " + tokens[j+2] + " " + tokens[j+3] + " ";
                            eBFtoString += tokenNames[10] + " " + tokenNames[11] + " " + tokenNames[12] + " " + tokenNames[13] + " ";
                            processedeBFCode += tokens[j] + " " + tokens[j+1] + " " + tokens[j+2] + " " + tokens[j+3] + " ";
                            j++;
                            break;
                        case "%":
                            eBinCode += eBinTokens[11] + " " + tokens[j+1] + " ";
                            eBFtoString += tokenNames[12] + " " + tokenNames[13] + " ";
                            processedeBFCode += tokens[j] + " " + tokens[j+1] + " ";
                            j++;
                            break;
                        case "$":
                            eBinCode += eBinTokens[12] + " " + tokens[j+1] + " " + tokens[j+2] + " " + tokens[j+3] + " ";
                            eBFtoString += tokenNames[13] + " " + tokenNames[14] + " ";
                            processedeBFCode += tokens[j] + " " + tokens[j+1] + " " + tokens[j+2] + " " + tokens[j+3] + " ";
                            j++;
                            break;
                        case "END":
                            eBinCode += eBinTokens[13];
                            eBFtoString += tokenNames[17];
                            processedeBFCode += tokens[j];
                            break;
                        default:
                            break;
                    }
                    break;
                }
            }
        }
        return eBinCode;
    }

    public static void main(String[] args){

        String eBFCode = "";

        // get eBF code from user
        if(args.length > 0){
            if(args[0].equals("-f")){
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
            
        } else {
            java.util.Scanner sc = new java.util.Scanner(System.in);
            System.out.print("CODE: ");
            eBFCode = sc.nextLine();
            System.out.println("");
            sc.close();
        }

        // add END token to eBF code if it doesn't already exist
        if(!eBFCode.endsWith("END")){ eBFCode += " END"; }
        
        // compile eBF code
        String eBinCode = compile(eBFCode);
        
        // output various stages of compilation
        System.out.println("eBF: " + eBFCode);
        System.out.println("eBin: " + eBinCode);
        System.out.println("Processed eBF: " + processedeBFCode);
        System.out.println("eBF Tokens: " + eBFtoString);
    }
}