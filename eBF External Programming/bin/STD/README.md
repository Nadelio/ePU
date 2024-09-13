# Standard Library Documentation

- `clearPointerValue`
  - clears the pointer value and the current RAM slot
- `popRAMValue`
  - moves the RAM value from the RAM slot being pointed at to the pointer value and sets the RAM slot to `0`
- `add`
  - adds the top two stack values together and stores them in the current RAM slot
  - this sets the RAM slot immediately to the right of it to `0` as well
- `space`
  - sets the pointer value and the current RAM slot to `32` (the character code for ` `)
- `zero`
  - sets the pointer value and the current RAM slot to `48` (the character code for `0`)
- `A`
  - sets the pointer value and the current RAM slot to `65` (the character code for `A`)
- `a`
  - sets the pointer value and current RAM slot to `97` (the character code for `a`)