use std::{fs::File, io::Read};

pub const ROM_SIZE: usize = u32::MAX as usize;

impl Rom {
    pub fn new(source: String) -> Rom {
        Rom {
            source: source,
            rom: Box::new([0; ROM_SIZE]),
            metadata: Box::new([0; ROM_SIZE]),
        }
    }

    pub fn load(&mut self) {
        // load the source file into the rom
        // source file is a set of 5 * ROM_SIZE bytes
        // each 5 bytes represents a data identifier, then a 32-bit word
        let src = File::open(self.source.clone()).expect("Could not open source file");
        let mut buf = [0; 4];
        let mut buf_index = 0;
        for (i, byte) in src.bytes().enumerate() {
            if i % 5 == 0 {
                let data = byte.unwrap();
                let addr = i / 5;
                self.metadata[addr] = data;
            } else {
                buf[buf_index] = byte.unwrap();
                buf_index += 1;
                if buf_index == 4 {
                    let addr = i / 5;
                    self.rom[addr] = u32::from_le_bytes(buf);
                    buf_index = 0;
                    buf = [0; 4];
                }
            }
        }
    }

    pub fn export(&self) {
        // export the rom back to the source file
        // source file is a set of 5 * ROM_SIZE bytes
        // each 5 bytes represents a data identifier, then a 32-bit word
        let src = File::open(self.source.clone()).expect("Could not open source file");
    }

    ///! also check if has proper permissions
    pub fn read(&self, addr: usize) -> Result<u32, &str> {
        // read a 32-bit word from the rom
        if addr >= ROM_SIZE {
            return Err("Address out of bounds");
        }

        let invis_check = self.metadata[addr] & 0b00000010;
        if invis_check == 1 {
            return Err("Read is not allowed on hidden memory without the proper permissions");
        }

        Ok(self.rom[addr])
    }

    pub fn write(&mut self, addr: usize, data: u32) -> Result<(), &str> {
        // write a 32-bit word to the rom
        if addr >= ROM_SIZE {
            return Err("Address out of bounds");
        }

        // check if the memory is protected
        let prot_check = self.metadata[addr] & 0b00000001;
        if prot_check == 1 {
            return Err("Write is not allowed on protected memory");
        }

        self.rom[addr] = data;
        return Ok(());
    }
}

pub struct Rom {
    source: String, // path to the source file
    rom: Box<[u32; ROM_SIZE]>,
    metadata: Box<[u8; ROM_SIZE]>,
}
