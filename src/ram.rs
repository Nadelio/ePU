pub const RAM_SIZE: usize = u32::MAX as usize;

impl Ram {
    pub fn new() -> Ram {
        Ram {
            ram: Box::new([0; RAM_SIZE]),
        }
    }

    pub fn flush(&mut self) {
        self.ram = Box::new([0; RAM_SIZE]);
    }

    pub fn read(&self, address: usize) -> Result<u32, String> {
        if address >= RAM_SIZE {
            return Err(format!("Address out of bounds: 0x{:X}", address));
        }
        return Ok(self.ram[address]);
    }

    pub fn write(&mut self, address: usize, data: u32) -> Result<(), String> {
        if address >= RAM_SIZE {
            return Err(format!("Address out of bounds: 0x{:X}", address));
        }
        self.ram[address] = data;
        return Ok(());
    }
}

pub struct Ram {
    ram: Box<[u32; RAM_SIZE]>,
}
