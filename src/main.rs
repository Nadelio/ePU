use chronos_vm::ram::*;
use chronos_vm::rom::*;
use chronos_vm::*;
use std::error::Error;
use std::fs::File;
use std::io::{self, Write};

fn main() -> Result<(), Box<dyn Error>> {
    let _f = File::open("test.rom").unwrap_or_else(|_| {
        let mut f = File::create("test.rom").unwrap();
        f.write_all(&[0; rom::ROM_SIZE]).unwrap();
        f
    });

    let mut rom = Rom::new("test.rom".to_string());

    let data = rom.read(0).unwrap_or(RomData {
        data: 0,
        metadata: 0,
    }); // should be {0, 0} after second run

    println!("Data: {:X}\nMetadata: {:X}", data.data, data.metadata);

    rom.write(
        0,
        RomData {
            data: 0x0,
            metadata: 0b00000001,
        },
    )
    .unwrap();

    let r = rom.read(0); // should err
    if r.is_err() {
        println!("Error reading from ROM: {:?}", r.err().unwrap());
    } else {
        let data = r.unwrap();
        println!("Data: {:X}\nMetadata: {:X}", data.data, data.metadata);
    }

    let mut vm_manager = VirtualMemoryManager::new();
    println!("Enter a virtual memory address in hex (e.g., 0x00401000) or 'q' to quit\n");

    loop {
        print!("> ");
        io::stdout().flush()?;
        let mut input = String::new();
        io::stdin().read_line(&mut input)?;
        let input = input.trim();

        if input == "q" {
            break;
        }

        let virtual_address = usize::from_str_radix(input.trim_start_matches("0x"), 16)?;

        print!("Allocate (a) or Deallocate (d)? ");
        io::stdout().flush()?;
        let mut action = String::new();
        io::stdin().read_line(&mut action)?;
        let action = action.trim().to_lowercase();

        match action.as_str() {
            "a" => {
                let physical_address = vm_manager.translate_address(virtual_address)?;
                println!(
                    "Virtual Address 0x{:X} -> Physical Address 0x{:X}",
                    virtual_address, physical_address
                );
            }
            "d" => {
                vm_manager.deallocate_address(virtual_address)?;
            }
            _ => println!("Invalid action. Use 'a' for allocate or 'd' for deallocate."),
        }
    }

    Ok(())
}
