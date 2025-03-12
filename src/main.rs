use std::fs::File;
use std::io::Write;

use chronos_vm::ram::*;
use chronos_vm::rom::*;

fn main() {

    if !std::path::Path::new("test.rom").exists() {
        let mut file = File::create("test.rom").expect("Could not create test.rom");
        file.write_all(&[0; ROM_SIZE*5]).expect("Could not write to test.rom");
    }

    // simple tests for RAM
    println!("0x{:X}", RAM_SIZE); // > 0xFFFFFFFF
    let mut ram = Ram::new();
    let _ = ram.write(0, 5); // (addr, data)
    println!("{}", ram.read(0).unwrap()); // > 5
    ram.flush();
    println!("{:?}", ram.write(0, 1).unwrap()); // (addr, data) // > Address out of bounds: 0xFFFFFFFF
    println!("{}", ram.read(0).unwrap()); // > Address out of bounds: 0xFFFFFFFF

    // simple tests for ROM
    let mut rom = Rom::new("test.rom".to_owned());
    rom.load();
    println!("{:?}", rom.read(0).unwrap()); // > 0
    println!("{:?}", rom.write_meta(1, 0b00000001).unwrap()); // > 0
    println!("{:?}", rom.write(1, 10).unwrap()); // > Write is not allowed on protected memory
    println!("{:?}", rom.write(0, 10).unwrap()); // > ()
    println!("{:?}", rom.read(0).unwrap()); // > 10
}