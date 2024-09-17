# eBF and eBin Documentation
- `+`: increment pointer value
- `-`: decrement pointer value
- `>`: increment pointer position
- `<`: decrement pointer position
- `,`: write to tape
- `.`: ask for character, store character in the current value of a cell being pointed at as `word`
- `=`: write to terminal the current value of the cell being pointed at as a character*
- `[`: begin conditional loop, if the current value of the cell being pointed at  is `0` when `[` is met, skip, otherwise, loop until the current value of the cell being pointed at is `0`
- `]`: end conditional loop
- `>>`: push current pointer value to stack, set pointer value to `0`
- `<<`: pop top value from stack and store in pointer value
- `'`: read from the current value of the cell being pointed at
- `DPND`: create a dependency using the next two tokens &rarr; `DPND <.ebf/.ebin file path> <alias>`
- `%`: call a dependency using its alias &rarr; `% <alias>`
- `END`: declare the end of a eBF program

- *printing to terminal adds 32 to the current cell value being pointed at before printing
- *printing to terminal follows the Java Character Code conventions

# Documentation Terminology
- Tape: Main memory unit, interacted with via the read and write instructions (`'`/`,`)
  - `[0][0][0][0]`
  - each piece of the Tape is called a "cell"
  - the size of the Tape is `((2^8)^2)-2`/`65534`
  - the size of a cell is a `word`
- Stack: secondary memory unit, interacted with via the push and pop instructions (`>>`/`<<`)
  - `[ 0, 0 ]`
  - each member of the Stack is called a "paper"
  - the size of the Stack is `256`
  - the size of a paper is a `word`
- Pointer value: The value stored with the pointer, interacted with via the increment and decrement instructions (`+`/`-`)
  - `PV = 0`
- Pointer position: The position in the Tape that the pointer is pointing at, interacted with via the increment and decrement pointer instructions (`>`/`<`)
  - represented as a `*` under a cell in the state diagram
- `word`: 16-bit unsigned integer

A *state diagram* is a diagram that shows a representation of the current state of an eBF program:
```
// Beginning state of all eBF programs
[0][0][0][0]
 *
[ 0, 0 ]
PV = 0
```
A *function notation* is a quick representation of the beginning state of a function:
```
 Top of stack  Bottom of stack
     \              /
% foo [ a, b, c... ]
   |     \ | /
   |  Stack parameters in stack order
Function name
```

# Getting Started
- ***The point of entry for using either the compiler or the interpreter is the `eBF.bash` file, so you will need a way to run said `eBF.bash` file***
- Put all 3 files (`eBF.bash`, `eBFCompiler.jar`, `eBFInterpreter.jar`) in a folder, then run the bash file using `bash eBF.bash -h`, this will give you all the flags, commands, and CLI syntax
- To run a `.ebf` file, use `bash eBF.bash -i --eBF <file>`
- To run a `.ebin` file, use `bash eBF.bash -i --eBin <file>`
- To compile a .ebf file, use `bash eBF.bash -c -f-o <file>`, this will output the binary file under the same file name but with the `.ebin` file extension
- To debug the interpreter, add the `-d` flag to the `--eBF`/`--eBin` flag like:
  - `--eBF-d`/`--eBin-d`
  - this works the same way with the compiler
