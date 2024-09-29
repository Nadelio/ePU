# TODO
- [X] &rarr; BIOS
- [X] &rarr; Control Unit
- [x] &rarr; Arithmetic Logic Unit
- [X] &rarr; RAM Unit
- [X] &rarr; ROM Unit
  - [X] &rarr; ROM Write Rules
  - [x] &rarr; Rewrite data dump method to look at a specific memory address range in RAM for data
  - [X] &rarr; size overflow rule/error
  - [X] &rarr; map RAM addresses for data dump
- [X] &rarr; Registers
- [X] &rarr; Screen Unit
  - [x] &rarr; Rewrite data dump method to look at a specific memory address range in RAM for data
  - [x] &rarr; size overflow rule/error
  - [X] &rarr; map RAM addresses for data dump
- [X] &rarr; Embedded eBF Interpreter
  - [X] &rarr; Update to work off of Words/UnsignedBytes
  - [X] &rarr; Make `$` take in 5 UnsignedBytes
  - [X] &rarr; Dependency Logic
  - [X] &rarr; Words &rarr; UnsignedBytes
  - [X] &rarr; Strings &rarr; UnsignedBytes
  Do not compile Aliases to bytes
- [X] &rarr; Embedded eBF Compiler
  - [X] &rarr; Compile to UnsignedBytes
  Do not compile Aliases to bytes
- [X] &rarr; Program Counter Unit

- [X] &rarr; Refactor everything to use `UnsignedByte` class