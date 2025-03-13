use chronos_vm::ram::*;
use chronos_vm::rom::*;
use chronos_vm::print_colors::*;
use std::error::Error;
use std::fs::File;
use std::io::{self, Write};

fn main() -> Result<(), Box<dyn Error>> {
    File::open("test.rom").unwrap_or_else(|_| {
        let mut f = File::create("test.rom").unwrap();
        f.write_all(&[0; ROM_SIZE]).unwrap(); // rom size was changed to 2048 bytes
        f
    });

    let mut rom = Rom::new("test.rom".to_string());

    println!("{}Testing ROM successful write...{}", DEBUG, RESET);
    let w = rom.write(0, RomData { data: 1, metadata: 0x3 }); // will fail on 1< run
    handle_write_result(w);

    println!("{}Testing ROM unsuccessful write...{}", DEBUG, RESET);
    let w = rom.write(0, RomData { data: 0, metadata: 0x0 });
    handle_write_result(w);

    println!("{}Testing ROM successful read...{}", DEBUG, RESET);
    let r = rom.read(1);
    handle_read_result(r);

    println!("{}Testing ROM unsuccessful read...{}", DEBUG, RESET);
    let r = rom.read(0);
    handle_read_result(r);

    // Test the RAM

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

fn handle_write_result(result: Result<(), &str>) {
    match result {
        Ok(_) => println!("{}Operation successful.{}", OK, RESET),
        Err(e) => println!("{}Error: {}{}", ERR, e, RESET),
    }
}

fn handle_read_result(result: Result<RomData, &str>) {
    match result {
        Ok(data) => println!("{}Data: {}0x{:X}\n{}Metadata: {}0x{:X}{}", OK, DATA, data.data, OK, DATA, data.metadata, RESET),
        Err(e) => println!("{}Error: {}{}", ERR, e, RESET),
    }
}
