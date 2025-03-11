use chronos_vm::ram::*;

fn main() {

    // simple tests for RAM
    println!("0x{:X}", RAM_SIZE); // > 0xFFFFFFFF
    let mut ram = Ram::new();
    ram.write(0, 5); // (addr, data)
    println!("{}", ram.read(0).unwrap()); // > 5
    ram.flush();
    println!("{:?}", ram.write(0, 1).unwrap()); // (addr, data) // > Address out of bounds: 0xFFFFFFFF
    println!("{}", ram.read(0).unwrap()); // > Address out of bounds: 0xFFFFFFFF

    // simple tests for ROM
    let mut rom = Rom::new();
    rom.load("test.rom", "protected.rom");
    println!("{:?}", rom.read(0).unwrap()); // > 0
    println!("{:?}", rom.write(1, 10).unwrap()); // > Write is not allowed on protected memory
    println!("{:?}", rom.write(0, 10).unwrap()); // > ()
    println!("{:?}", rom.read(0).unwrap()); // > 10
}