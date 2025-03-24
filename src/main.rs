use chronos_vm::ram::info;
use chronos_vm::ram::*;
use chronos_vm::rom::*;
use colored::*;
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

    println!("{}", "Testing ROM successful write...".yellow());
    let w = rom.write(
        0,
        RomData {
            data: 1,
            typedata: 0x3,
        },
    ); // will fail on 1< run
    handle_write_result(w);

    println!("{}", "Testing ROM unsuccessful write...".yellow());
    let w = rom.write(
        0,
        RomData {
            data: 0,
            typedata: 0x0,
        },
    );
    handle_write_result(w);

    println!("{}", "Testing ROM successful read...".yellow());
    let r = rom.read(1);
    handle_read_result(r);

    println!("{}", "Testing ROM unsuccessful read...".yellow());
    let r = rom.read(0);
    handle_read_result(r);
    info();
    let mut machine = Machine::new();
    machine.create_bitmap()?;
    for _ in 0..=16 {
        println!("Allocated a page at 0x{:x}", machine.alloc()?);
    }

    // Change these addresses to generate an access violation/segfault
    machine.dealloc(0x24000)?;
    println!("Allocated a page at 0x{:x}", machine.alloc()?);
    machine.write(0x24000, 33)?;
    println!("Read a value of {}", machine.read(0x24000)?);

    // Test the RAM
    let mut vm_manager = VirtualMemoryManager::new();
    let trans_count = u32::MAX as usize;
    println!(
        "Performing {} translations... this may take some time",
        format_with_commas(trans_count)
    );
    let time = std::time::Instant::now();
    for i in 0..trans_count {
        vm_manager.translate_address(i)?;
    }
    println!(
        "{} translations took {:?}",
        format_with_commas(trans_count),
        time.elapsed()
    );

    println!("Enter a virtual memory address in hex (e.g., 0x00401000) or 'q' to quit\n");

    let mut vm_manager = VirtualMemoryManager::new();
    loop {
        print!("> ");
        io::stdout().flush()?;
        let mut input = String::new();
        io::stdin().read_line(&mut input)?;
        let input = input.trim();

        if input == "q" {
            break;
        }
        let mut keep_going = false;
        let virtual_address = u32::from_str_radix(input.trim_start_matches("0x"), 16)
            .unwrap_or_else(|e| {
                keep_going = true;
                println!("{e}");
                0
            });
        if keep_going {
            continue;
        }

        print!("Write (w), Read (r), Print State (p), or Deallocate (d)? ");
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
            "r" => println!(
                "Value at 0x{:X}: {}",
                virtual_address,
                vm_manager.read_memory(virtual_address as usize)?
            ),

            "d" => vm_manager.deallocate_address(virtual_address as usize)?,
            "p" => {
                vm_manager.print_state();
                continue;
            }
            _ => println!("Invalid action."),
        }
        let physical_address = vm_manager.translate_address(virtual_address as usize)?;
        println!(
            "Virtual Address 0x{:X} -> Physical Address 0x{:X}",
            virtual_address, physical_address
        );
    }

    Ok(())
}

fn handle_write_result(result: Result<(), &str>) {
    match result {
        Ok(_) => println!("{}", "Operation successful.".green()),
        Err(e) => println!("{}: {}", "Error".red(), e),
    }
}

fn handle_read_result(result: Result<RomData, &str>) {
    match result {
        Ok(data) => println!(
            "{}: 0x{:X}\n{}: 0x{:X}",
            "Data".green(),
            data.data,
            "Typedata".green(),
            data.typedata
        ),
        Err(e) => println!("{}: {}", "Error".red(), e),
    }
}

fn format_with_commas(n: usize) -> String {
    let mut s = n.to_string();
    let len = s.len();

    for i in (1..len).rev() {
        if (len - i) % 3 == 0 {
            s.insert(i, ',');
        }
    }

    s
}
