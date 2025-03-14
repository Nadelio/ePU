use std::collections::HashSet;
use std::error::Error;

// There is ONE PD - Page Directory
// 
// The PD has 1024 entries
//
// Each PD entry (a PT) has 1024 entries for individual pages
//
// Each page stores 4096 bytes of information
//
// 4096 * 1024 * 1024 = 2 ^ 32
//
pub const PAGE_SIZE: usize = 4096;
pub const PAGE_TABLE_ENTRIES: usize = 1024;
pub const DIRECTORY_ENTRIES: usize = 1024;


/* Notes on comment abbreviations:
*
* (P)age (D)irectory - The complete directory of all pages
* (P)age (T)able - A singular entry in the PD, 
*   containing a vector of addresses and a present states
* (P)age (D)irectory (I)ndex - An offset to an index in the PD
* (P)age (T)able (I)ndex - An offset to an index in the PT
*/


/* Note on why there isn't PhysMem -> VMem
*
*
* Physical memory is not aware of virtual memory's existence, thus
* physical memory cannot be translated back to virtual memory addresses
*/


/* Note on usage of `usize`:
*
* Throughout this file, `usize` is used instead of `u32`, as it will create less casts to index
* into vectors and arrays. `usize` also allows for more modular code, as the bit-width of the
* machine can be easily adapted to larger or smaller sizes if needed, without introducing/removing
* many type casts
*/

#[derive(Debug, Clone)]
pub struct PageTableEntry {

    // A PhysMem address for the frame
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
    // TODO: This will hold a fixed number of entries
    // We can optimize it to be a heap array, but for now, I'll leave it as a vector
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
    // TODO: This is also a fixed entry count, and can be changed to a heap array
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
    frames: Vec<Option<Vec<u8>>>, // these are values at addresses
    allocated_frames: HashSet<usize>, // these are addresses of frames in use
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
        self.frames.push(Some(vec![0; PAGE_SIZE])); // By default, push a bunch of 0's to a new
        // The HashSet data structure should not have duplicates
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

    // (P)age (T)able (I)ndex
    // (P)age (D)irectory (I)ndex
    pub fn translate_address(&mut self, virtual_address: usize) -> Result<usize, Box<dyn Error>> {

        // Mask and bitshift to find the indices

        /*
        * 
        *
        * Example PDI and PTI retrieval from an address
        * 
        * Address:
        * +------------+
        * | 0x33221100 |
        * +------------+
        *
        * Let's expand this address
        *
        * +----------------------------------+
        * | 0b110011001000100001000100000000 |
        * +----------------------------------+
        * 
        * This is our full, 32-bit wide address in binary
        *
        * Let's expand the parts of this address
        *
        *     │ 00 1100 1100 │ 10 0010 0001 │ 0001 0000 0000 │
        *     ├─^^─^^^^─^^^^ ├─^^─^^^^─^^^^ ├─^^^^─^^^^─^^^^──────────────┐
        *     │              │              │  This is the offset         │
        *     │              │              │  of the address in the page │
        *     │              │              └─────────────────────────────┘
        *     │              │
        *     │              ├────────────────────────────────────────────┐
        *     │              │  This is the offset in a page table record │
        *     │              │  for the specific page (4KB)               │
        *     │              └────────────────────────────────────────────┘
        *     │
        * ┌───┴──────────────────────────────────────────────────────────────┐
        * │  This is the offset in the Page Directory for the exact entry.   │
        * │  The offset contains a specific page table record, which is used │
        * │  to then find the specific page that the address resides at.     │
        * └──────────────────────────────────────────────────────────────────┘
        */ 
        let pdi = (virtual_address >> 22) & 0x3FF;
        let pti = (virtual_address >> 12) & 0x3FF;
        let offset = virtual_address & 0xFFF;

        // Check if the Page Directory has registered this PDI
        if self.page_directory.tables[pdi].is_none() {
            // Make a new one
            self.page_directory.tables[pdi] = Some(PageTable::new());
        }

        // The PD will have this PDI registered now
        let page_table = self.page_directory.tables[pdi].as_mut().unwrap();

        // Register the PTI info in the PT if it has not been set
        if !page_table.entries[pti].present {
            // Create a new frame in PhysMem
            let frame_address = self.physical_memory.allocate_frame();

            // Set the entry to have the frame address we just retrieved
            page_table.entries[pti].frame_address = Some(frame_address);
            page_table.entries[pti].present = true;
        }

        // We now look through the PT and find the PhysMem frame address
        let frame_address = page_table.entries[pti].frame_address.unwrap();
        Ok(frame_address + offset)
    }

    pub fn deallocate_address(&mut self, virtual_address: usize) -> Result<(), Box<dyn Error>> {
        // Mask and bitshift to find indices
        let pdi = (virtual_address >> 22) & 0x3FF;
        let pti = (virtual_address >> 12) & 0x3FF;

        // This PDI has not been allocated
        if self.page_directory.tables[pdi].is_none() {
            return Ok(());
        }

        let page_table = self.page_directory.tables[pdi].as_mut().unwrap();

        // This PTI has not been allocated
        if !page_table.entries[pti].present {
            return Ok(());
        }

        let frame_address = page_table.entries[pti].frame_address.unwrap();
        self.physical_memory.deallocate_frame(frame_address)?;
        page_table.entries[pti].present = false;
        page_table.entries[pti].frame_address = None;

        // If everything in this PTI is empty, we will free it from the PD
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
