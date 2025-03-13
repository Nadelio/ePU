use chronos_vm::ram::*;
use std::error::Error;
use std::io::{self, Write};
fn main() -> Result<(), Box<dyn Error>> {
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
