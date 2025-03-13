use std::error::Error;

pub const RAM_SIZE: usize = u32::MAX as usize;
pub const PAGE_SIZE: usize = 4096;
pub const PAGE_TABLE_ENTRIES: usize = 1024;
pub const DIRECTORY_ENTRIES: usize = 1024;

#[derive(Debug, Clone)]
pub struct PageTableEntry {
    frame_address: Option<usize>,
    present: bool,
}

impl PageTableEntry {
    fn new() -> Self {
        PageTableEntry {
            frame_address: None,
            present: false,
        }
    }
}

#[derive(Debug, Clone)]
struct PageTable {
    entries: Vec<PageTableEntry>,
}

impl PageTable {
    fn new() -> Self {
        PageTable {
            entries: vec![PageTableEntry::new(); PAGE_TABLE_ENTRIES],
        }
    }
}

#[derive(Debug)]
struct PageDirectory {
    tables: Vec<Option<PageTable>>,
}

impl PageDirectory {
    fn new() -> Self {
        PageDirectory {
            tables: vec![None; DIRECTORY_ENTRIES],
        }
    }
}

#[derive(Debug)]
struct PhysicalMemory {
    frames: Vec<Option<Vec<u8>>>,
}

impl PhysicalMemory {
    fn new() -> Self {
        PhysicalMemory { frames: Vec::new() }
    }

    fn allocate_frame(&mut self) -> usize {
        let frame_address = self.frames.len() * PAGE_SIZE;
        self.frames.push(Some(vec![0; PAGE_SIZE]));
        frame_address
    }

    fn deallocate_frame(&mut self, frame_address: usize) -> Result<(), Box<dyn Error>> {
        let frame_index = frame_address / PAGE_SIZE;
        if frame_index >= self.frames.len() || self.frames[frame_index].is_none() {
            return Err("Invalid frame address".into());
        }
        self.frames[frame_index] = None;
        Ok(())
    }
}

#[derive(Debug)]
pub struct VirtualMemoryManager {
    page_directory: PageDirectory,
    physical_memory: PhysicalMemory,
}

impl Default for VirtualMemoryManager {
    fn default() -> Self {
        Self::new()
    }
}

impl VirtualMemoryManager {
    pub fn new() -> Self {
        VirtualMemoryManager {
            page_directory: PageDirectory::new(),
            physical_memory: PhysicalMemory::new(),
        }
    }

    pub fn translate_address(&mut self, virtual_address: usize) -> Result<usize, Box<dyn Error>> {
        println!("\nTranslating virtual address: 0x{:X}", virtual_address);

        let pdi = (virtual_address >> 22) & 0x3FF;
        let pti = (virtual_address >> 12) & 0x3FF;
        let offset = virtual_address & 0xFFF;

        println!("Step 1: Extracting indices from the virtual address.");
        println!("  - Virtual Address: 0x{:X}", virtual_address);
        println!("  - Page Directory Index (PDI): 0x{:X} (Bits 31-22)", pdi);
        println!("  - Page Table Index (PTI): 0x{:X} (Bits 21-12)", pti);
        println!("  - Offset: 0x{:X} (Bits 11-0)", offset);

        if self.page_directory.tables[pdi].is_none() {
            println!(
                "Step 2: Page Directory Entry (PDE) at index 0x{:X} is empty.",
                pdi
            );
            println!("  - Allocating a new Page Table for this PDE.");
            self.page_directory.tables[pdi] = Some(PageTable::new());
        }

        let page_table = self.page_directory.tables[pdi].as_mut().unwrap();

        if !page_table.entries[pti].present {
            println!(
                "Step 3: Page Table Entry (PTE) at index 0x{:X} is empty.",
                pti
            );
            println!("  - Allocating a new Physical Frame for this PTE.");
            let frame_address = self.physical_memory.allocate_frame();
            page_table.entries[pti].frame_address = Some(frame_address);
            page_table.entries[pti].present = true;
        }

        let frame_address = page_table.entries[pti].frame_address.unwrap();
        let physical_address = frame_address + offset;

        println!("Step 4: Calculating the Physical Address.");
        println!("  - Frame Address: 0x{:X}", frame_address);
        println!("  - Offset: 0x{:X}", offset);
        println!(
            "  - Physical Address: 0x{:X} + 0x{:X} = 0x{:X}",
            frame_address, offset, physical_address
        );

        Ok(physical_address)
    }

    pub fn deallocate_address(&mut self, virtual_address: usize) -> Result<(), Box<dyn Error>> {
        println!("\nDeallocating virtual address: 0x{:X}", virtual_address);

        let pdi = (virtual_address >> 22) & 0x3FF;
        let pti = (virtual_address >> 12) & 0x3FF;

        println!("Step 1: Extracting indices from the virtual address.");
        println!("  - Virtual Address: 0x{:X}", virtual_address);
        println!("  - Page Directory Index (PDI): 0x{:X} (Bits 31-22)", pdi);
        println!("  - Page Table Index (PTI): 0x{:X} (Bits 21-12)", pti);

        if self.page_directory.tables[pdi].is_none() {
            println!(
                "Step 2: Page Directory Entry (PDE) at index 0x{:X} is empty.",
                pdi
            );
            println!("  - Nothing to deallocate.");
            return Ok(());
        }

        let page_table = self.page_directory.tables[pdi].as_mut().unwrap();

        if !page_table.entries[pti].present {
            println!(
                "Step 3: Page Table Entry (PTE) at index 0x{:X} is already deallocated.",
                pti
            );
            println!("  - Nothing to deallocate.");
            return Ok(());
        }

        let frame_address = page_table.entries[pti].frame_address.unwrap();
        self.physical_memory.deallocate_frame(frame_address)?;
        page_table.entries[pti].present = false;
        page_table.entries[pti].frame_address = None;

        println!(
            "Step 4: Deallocated PTE at index 0x{:X} in Page Table at PDI 0x{:X}.",
            pti, pdi
        );
        println!(
            "  - Frame Address: 0x{:X} has been deallocated.",
            frame_address
        );

        // Optional: Free the entire Page Table if all PTEs are deallocated
        if page_table.entries.iter().all(|entry| !entry.present) {
            self.page_directory.tables[pdi] = None;
            println!(
                "  - Page Table at PDI 0x{:X} is now empty and has been freed.",
                pdi
            );
        }

        Ok(())
    }
}
