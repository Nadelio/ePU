//(1024 x 768) x 2 ints = 1,572,864 bytes

pub const SCREEN_SIZE: [usize; 2] = [512, 384];
pub const ASPECT_RATIO: [u32; 2] = [4, 3];
pub const BUFFER_PAGES: u32 = 2;
pub const PAGE_STARTS: [usize; 2] = [0, SCREEN_SIZE[0] * SCREEN_SIZE[1]];
pub const SCREEN_BUFFER_SIZE: usize = SCREEN_SIZE[0] * SCREEN_SIZE[1] * 2;

impl Default for ScreenBuffer {
    fn default() -> Self {
        Self::new()
    }
}

impl ScreenBuffer {
    pub fn new() -> ScreenBuffer {
        ScreenBuffer {
            buffer: Box::new([0; SCREEN_BUFFER_SIZE]),
            page: 0,
        }
    }

    pub fn switch_page(&mut self) {
        self.page = (self.page + 1) % BUFFER_PAGES;
    }

    pub fn read(&self, address: usize) -> Result<u32, String> {
        if address >= SCREEN_BUFFER_SIZE {
            return Err(format!("Address out of bounds: 0x{:X}", address));
        }
        Ok(self.buffer[address])
    }

    pub fn write(&mut self, address: usize, data: u32) -> Result<(), String> {
        if address >= SCREEN_BUFFER_SIZE {
            return Err(format!("Address out of bounds: 0x{:X}", address));
        }
        self.buffer[address] = data;
        Ok(())
    }

    /// push the current page to the screen* (screen currently not implemented)
    pub fn push_to_screen(&self) {}

    pub fn clear(&mut self) {
        self.buffer = Box::new([0; SCREEN_BUFFER_SIZE]);
    }
}

pub struct ScreenBuffer {
    buffer: Box<[u32; SCREEN_BUFFER_SIZE]>,
    page: u32,
}
