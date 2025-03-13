pub const RAM_SIZE: usize = 2048 as usize;
pub const INPUT_BUFFER_DIM: [usize; 2] = [0, 64];

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
