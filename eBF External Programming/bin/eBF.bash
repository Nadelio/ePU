if [ "$1" == "-c" ]; then
    java -jar --enable-preview eBFCompiler.jar $2 $3
elif [ "$1" == "-i" ]; then
    java -jar --enable-preview eBFInterpreter.jar $2 $3
else
    if [ "$1" == "-h" ] || [ "$1" == "--help" ]; then
        echo "Usage: eBF [-c|-i] <secondary flags> <file>"
        echo "  -c: Compile the source file to the output file"
        echo "  -i: Interpret the source file"
        echo " "
        echo "Secondary flags:"
        echo " "
        echo "  Compiler Specific flags:"
        echo "      -f: read from file, requires a file path following it"
        echo " "
        echo "  Interpreter Specific flags:"
        echo "      --eBF: run eBF file, requires a file path following it"
        echo "      --eBIN: run eBIN file, requires a file path following it"
        echo "      --help | -h: show interpreter specific help message"
    fi
fi