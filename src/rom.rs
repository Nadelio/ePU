use std::{
    fs::{File, OpenOptions},
    io::{Read, Seek, SeekFrom, Write},
};

pub const ROM_SIZE: usize = 2048 as usize;

/// p? = program data? ; data type (3 bits) ; size (in bytes) (2 bits) ; r?: read allowed? ; w?: write allowed?
pub const TYPE_AND_DATA_BYTE_FORMAT: &str = "[p?][type][size][r?][w?]";

pub struct Rom {
    source: String, // path to the source file
}

#[derive(Clone)]
pub struct RomData {
    pub data: u32,
    pub metadata: u8,
}

impl Rom {
    pub fn new(source: String) -> Rom {
        Rom { source }
    }

    pub fn read_many(&self, addr: usize, size: usize) -> Result<Vec<RomData>, &str> {
        if addr >= ROM_SIZE || addr + size >= ROM_SIZE {
            return Err("Address out of bounds");
        }

        let lit_addr = addr * 5;
        let mut file = File::open(&self.source).unwrap();
        file.seek(SeekFrom::Start(lit_addr as u64)).unwrap();
        let mut data_buf = vec![
            RomData {
                data: 0,
                typedata: 0
            };
            size
        ];

        for i in 0..size {
            let r = self.read(lit_addr + i);
            if r.is_err() {
                return Err(r.err().unwrap());
            }
            let d = r.unwrap();
            if d.metadata & 0b00000010 != 0 {
                return Err("Read is not allowed on hidden memory without the proper permissions");
            }
            data_buf[i] = d;
        }

        Ok(data_buf)
    }

    pub fn read(&self, addr: usize) -> Result<RomData, &str> {
        if addr >= ROM_SIZE {
            return Err("Address out of bounds");
        }

        let lit_addr = addr * 5;
        let mut file = File::open(&self.source).unwrap();
        file.seek(SeekFrom::Start(lit_addr as u64)).unwrap();
        let mut buffer = [0; 5];
        file.read_exact(&mut buffer).unwrap();

        let td: u8 = buffer[0];

        if td & 0b00000010 != 0 {
            return Err("Read is not allowed on hidden memory without the proper permissions");
        }

        let d = u32::from_be_bytes([buffer[1], buffer[2], buffer[3], buffer[4]]);

        Ok(RomData {
            data: d,
            typedata: td,
        })
    }

    pub fn write_many(&mut self, addr: usize, data: Vec<RomData>, size: usize) -> Result<(), &str> {
        if addr >= ROM_SIZE || addr + size >= ROM_SIZE {
            return Err("Address out of bounds");
        }

        let mut f = OpenOptions::new().write(true).open(&self.source).unwrap();
        f.seek(SeekFrom::Start((addr * 5) as u64)).unwrap();

        for i in 0..size {
            let r = self.read(addr + i);
            if r.is_err() {
                return Err(r.err().unwrap());
            }
            if r.unwrap().metadata & 0b00000001 != 0 {
                return Err("Write is not allowed on protected memory");
            }

            let mut buf = [0u8; 5];
            buf[0] = data[i].typedata;
            buf[1..5].copy_from_slice(&data[i].data.to_be_bytes());
            f.write_all(&buf).unwrap();
        }

        Ok(())
    }

    pub fn write(&mut self, addr: usize, data: RomData) -> Result<(), &str> {
        if addr >= ROM_SIZE {
            return Err("Address out of bounds");
        }

        let r = self.read(addr);
        if r.is_err() {
            return Err(r.err().unwrap());
        }
        if r.unwrap().typedata & 0b00000001 != 0 {
            return Err("Write is not allowed on protected memory");
        }

        let mut f = OpenOptions::new().write(true).open(&self.source).unwrap();
        f.seek(SeekFrom::Start((addr * 5) as u64)).unwrap();

        let mut buf = [0u8; 5];
        buf[0] = data.typedata;
        buf[1..5].copy_from_slice(&data.data.to_be_bytes());
        f.write_all(&buf).unwrap();

        Ok(())
    }
}

pub struct Rom {
    source: String, // path to the source file
}

#[derive(Clone)]
pub struct RomData {
    pub data: u32,
    pub typedata: u8
}
