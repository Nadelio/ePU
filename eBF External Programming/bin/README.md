# eBF and eBin Documentation
- `+`: increment pointer value
- `-`: decrement pointer value
- `>`: increment pointer position
- `<`: decrement pointer position
- `,`: write to RAM
- `.`: ask for character, store character in the current value of the RAM being pointed at as `word`
- `=`: write to terminal the current value of the RAM being pointed at as a character*
- `[`: begin conditional loop, if the current value of the RAM being pointed at  is `0` when `[` is met, skip, otherwise, loop until the current value of the RAM being pointed at is `0`
- `]`: end conditional loop
- `>>`: push current pointer value to stack, set pointer value to `0`
- `<<`: pop top value from stack and store in pointer value
- `'`: read from the current value of the RAM being pointed at
- `DPND`: create a dependency using the next two tokens &rarr; `DPND <.ebf/.ebin file path> <alias>`
- `%`: call a dependency using its alias &rarr; `% <alias>`
- `END`: declare the end of a eBF program

- *printing to terminal adds 32 to the current RAM value being pointed at before printing
- *printing to terminal follows the Java Character Code conventions

# Getting Started
- ***The point of entry for using either the compiler or the interpreter is the `eBF.bash` file, so you will need a way to run said `eBF.bash` file***
- Put all 3 files (`eBF.bash`, `eBFCompiler.jar`, `eBFInterpreter.jar`) in a folder, then run the bash file using `bash eBF.bash -h`, this will give you all the flags, commands, and CLI syntax
- To run a `.ebf` file, use `bash eBF.bash -i --eBF <file>`
- To run a `.ebin` file, use `bash eBF.bash -i --eBin <file>`
- To compile a .ebf file, use `bash eBF.bash -c -f <file>`, this will output the binary version of the .ebf file into the terminal, I am currently working on adding output file options to the compiler 
