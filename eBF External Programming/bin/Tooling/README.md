# Getting Started
- ***The point of entry for the various tools are the bash files***
  - This is to dummy proof the tools
- `WriteToROM` has two arguments: input file, and output file
  - The input file is going to be the eBin file given to you by the EmbeddedeBFCompiler (in most cases)
  - The output file will be the `raw.rom` file and/or your `protected.rom` file, depending on what you want to edit
- `EmbeddedeBFCompiler` has the exact same arguments and flags as the non-embedded [eBFCompiler](https://github.com/Nadelio/ePU/tree/main/eBF%20External%20Programming/bin/README.md), only difference is its output format
  - the output `.ebin` file made by the `EmbeddedeBFCompiler` *cannot* be run by the `eBFInterpreter` due to the data format