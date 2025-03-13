use std::{
    fs::File, io::{Read, Seek, Write}
};

pub const ROM_SIZE: usize = u32::MAX as usize;
/// p? = program data?\n id = data identifier (3 bits)\n +/- = sign\n 0?: null? ; r?: read allowed? ; w?: write allowed?
pub const METADATA_FORMAT: &str = "[p?][id][+/-][0?][r?][w?]";

impl Rom {
    pub fn new(source: String) -> Rom {
        Rom {
            source
        }
    }

    pub fn read_many(&self, addr: usize, size: usize) -> Result<Vec<RomData>, &str>{

        if addr >= ROM_SIZE { return Err("Address out of bounds"); }
        if addr + size >= ROM_SIZE { return Err("Address out of bounds"); }

        let lit_addr = addr * 5;

        let mut file = File::open(&self.source).unwrap();
        file.seek(std::io::SeekFrom::Start(lit_addr as u64)).unwrap();
        let mut data_buf = vec![RomData { data: 0, metadata: 0 }; size];

        for i in 0..size { data_buf[i] = self.read(lit_addr + i).unwrap_or(RomData {data: 0, metadata: 0}); }

        return Ok(data_buf);
    }

    /// also check if has proper permission level (OS level) (currently not implemented)
    pub fn read(&self, addr: usize) -> Result<RomData, &str> {
        if addr >= ROM_SIZE { return Err("Address out of bounds"); }

        let lit_addr = addr * 5;

        let mut file = File::open(&self.source).unwrap();
        file.seek(std::io::SeekFrom::Start(lit_addr as u64)).unwrap();
        let mut buffer = [0; 5];
        file.read_exact(&mut buffer).unwrap();
        let md: u8 = buffer[0];

        let invis_check = md & 0b00000010;
        if invis_check == 1 { return Err("Read is not allowed on hidden memory without the proper permissions"); }

        let d = u32::from_be_bytes([buffer[1], buffer[2], buffer[3], buffer[4]]);

        return Ok(RomData { data: d, metadata: md });
    }

    pub fn write_many(&mut self, addr: usize, data: Vec<RomData>, size: usize) -> Result<(), &str> {
        // write a 32-bit word to the rom
        if addr >= ROM_SIZE { return Err("Address out of bounds"); }
        if addr + size >= ROM_SIZE { return Err("Address out of bounds"); }

        let mut f = File::open(&self.source).unwrap();
        f.seek(std::io::SeekFrom::Start((addr * 5) as u64)).unwrap();

        for i in 0..size {
            let mut buf = [0u8; 5];
            buf[0] = data[i].metadata;
            buf[1..5].copy_from_slice(&data[i].data.to_be_bytes());
            f.write(&buf).unwrap();
        }

        return Ok(());
    }

    pub fn write(&mut self, addr: usize, data: RomData) -> Result<(), &str> {
        // write a 32-bit word to the rom
        if addr >= ROM_SIZE {
            return Err("Address out of bounds");
        }

        // check if the memory is protected
        let prot_check = data.metadata & 0b00000001;
        if prot_check == 1 {
            return Err("Write is not allowed on protected memory");
        }

        let mut f = File::open(&self.source).unwrap();
        f.seek(std::io::SeekFrom::Start((addr * 5) as u64)).unwrap();
        
        let mut buf = [0u8; 5];
        buf[0] = data.metadata;
        buf[1..5].copy_from_slice(&data.data.to_be_bytes());
        f.write(&buf).unwrap();

        Ok(())
    }
}

pub struct Rom {
    source: String, // path to the source file
}

#[derive(Clone)]
pub struct RomData {
    pub data: u32,
    pub metadata: u8
}
