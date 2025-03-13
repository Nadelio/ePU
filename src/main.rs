use chronos_vm::print_colors::*;
use chronos_vm::ram::*;
use chronos_vm::rom::*;
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
    let w = rom.write(
        0,
        RomData {
            data: 1,
            metadata: 0x3,
        },
    ); // will fail on 1< run
    handle_write_result(w);

    println!("{}Testing ROM unsuccessful write...{}", DEBUG, RESET);
    let w = rom.write(
        0,
        RomData {
            data: 0,
            metadata: 0x0,
        },
    );
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

        let virtual_address = u32::from_str_radix(input.trim_start_matches("0x"), 16)?;

        print!("Write (w), Read (r), Allocate (a) or Deallocate (d)? ");
        io::stdout().flush()?;
        let mut action = String::new();
        io::stdin().read_line(&mut action)?;
        let action = action.trim().to_lowercase();

        match action.as_str() {
            "w" => {
                print!("Enter byte value (0-255): ");
                io::stdout().flush()?;
                let mut value = String::new();
                io::stdin().read_line(&mut value)?;
                let value: u8 = value.trim().parse()?;
                vm_manager.write_memory(virtual_address as usize, value)?;
            }
            "r" => {
                let value = vm_manager.read_memory(virtual_address as usize)?;
                println!("Value at 0x{:X}: {}", virtual_address, value);
            }
            _ => println!("Invalid action."),
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
        Ok(data) => println!(
            "{}Data: {}0x{:X}\n{}Metadata: {}0x{:X}{}",
            OK, DATA, data.data, OK, DATA, data.metadata, RESET
        ),
        Err(e) => println!("{}Error: {}{}", ERR, e, RESET),
    }
}
