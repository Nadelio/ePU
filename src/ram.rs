use std::collections::HashSet;
use std::error::Error;

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
    allocated_frames: HashSet<usize>,
}

impl PhysicalMemory {
    fn new() -> Self {
        PhysicalMemory {
            frames: Vec::new(),
            allocated_frames: HashSet::new(),
        }
    }

    fn allocate_frame(&mut self) -> usize {
        let frame_address = self.frames.len() * PAGE_SIZE;
        self.frames.push(Some(vec![0; PAGE_SIZE]));
        self.allocated_frames.insert(frame_address);
        frame_address
    }

    fn deallocate_frame(&mut self, frame_address: usize) -> Result<(), Box<dyn Error>> {
        let frame_index = frame_address / PAGE_SIZE;
        if frame_index >= self.frames.len() || self.frames[frame_index].is_none() {
            return Err("Invalid frame address".into());
        }
        self.frames[frame_index] = None;
        self.allocated_frames.remove(&frame_address);
        Ok(())
    }

    fn read(&self, frame_address: usize, offset: usize) -> Result<u8, Box<dyn Error>> {
        let frame_index = frame_address / PAGE_SIZE;
        if let Some(frame) = &self.frames[frame_index] {
            Ok(frame[offset])
        } else {
            Err("Invalid read operation: Frame not allocated".into())
        }
    }

    fn write(
        &mut self,
        frame_address: usize,
        offset: usize,
        value: u8,
    ) -> Result<(), Box<dyn Error>> {
        let frame_index = frame_address / PAGE_SIZE;
        if let Some(frame) = &mut self.frames[frame_index] {
            frame[offset] = value;
            Ok(())
        } else {
            Err("Invalid write operation: Frame not allocated".into())
        }
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
        let pdi = (virtual_address >> 22) & 0x3FF;
        let pti = (virtual_address >> 12) & 0x3FF;
        let offset = virtual_address & 0xFFF;

        if self.page_directory.tables[pdi].is_none() {
            self.page_directory.tables[pdi] = Some(PageTable::new());
        }

        let page_table = self.page_directory.tables[pdi].as_mut().unwrap();

        if !page_table.entries[pti].present {
            let frame_address = self.physical_memory.allocate_frame();
            page_table.entries[pti].frame_address = Some(frame_address);
            page_table.entries[pti].present = true;
        }

        let frame_address = page_table.entries[pti].frame_address.unwrap();
        Ok(frame_address + offset)
    }

    pub fn deallocate_address(&mut self, virtual_address: usize) -> Result<(), Box<dyn Error>> {
        let pdi = (virtual_address >> 22) & 0x3FF;
        let pti = (virtual_address >> 12) & 0x3FF;

        if self.page_directory.tables[pdi].is_none() {
            return Ok(());
        }

        let page_table = self.page_directory.tables[pdi].as_mut().unwrap();

        if !page_table.entries[pti].present {
            return Ok(());
        }

        let frame_address = page_table.entries[pti].frame_address.unwrap();
        self.physical_memory.deallocate_frame(frame_address)?;
        page_table.entries[pti].present = false;
        page_table.entries[pti].frame_address = None;

        if page_table.entries.iter().all(|entry| !entry.present) {
            self.page_directory.tables[pdi] = None;
        }

        Ok(())
    }
    pub fn write_memory(
        &mut self,
        virtual_address: usize,
        value: u8,
    ) -> Result<(), Box<dyn Error>> {
        let physical_address = self.translate_address(virtual_address)?;
        let offset = virtual_address & 0xFFF; // offset with v. addr
        let frame_address = physical_address - offset; // preventing collisions
        self.physical_memory.write(frame_address, offset, value)
    }

    pub fn read_memory(&mut self, virtual_address: usize) -> Result<u8, Box<dyn Error>> {
        let physical_address = self.translate_address(virtual_address)?;
        let offset = virtual_address & 0xFFF;
        let frame_address = physical_address - offset;
        self.physical_memory.read(frame_address, offset)
    }

    pub fn print_state(&self) {
        println!("=== Virtual Memory Manager State ===");

        println!("Page Directory:");
        for (pdi, table) in self.page_directory.tables.iter().enumerate() {
            if let Some(page_table) = table {
                println!("  PDE 0x{:03X} -> Page Table Exists", pdi);
                for (pti, entry) in page_table.entries.iter().enumerate() {
                    if entry.present {
                        let frame = entry.frame_address.unwrap();
                        println!("    PTE 0x{:03X} -> Frame 0x{:08X}", pti, frame);
                    }
                }
            }
        }

        println!("\nPhysical Memory:");
        if self.physical_memory.allocated_frames.is_empty() {
            println!("  No frames allocated.");
        } else {
            for &frame in &self.physical_memory.allocated_frames {
                println!("  Frame Address: 0x{:08X}", frame);
            }
        }
    }
}
