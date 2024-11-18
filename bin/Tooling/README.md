# Getting Started
- ***The point of entry for the various tools are the bash files***
  - This is to dummy proof the tools
- `WriteToROM` has three arguments: input program, x cordinate, and y cordinate
  - The input file is going to be the eBin file given to you by the eBFCompiler (in most cases)
  - The x and y cordinates will be first cordinates you want to write to in the ROM
  - The tool will automatically find the ROM data in the correct folder (./ePUx16SimData), unless it has been moved from the folder