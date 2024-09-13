# Standard Library Documentation

- `clearPointerValue`
  - clears the pointer value and the current RAM slot
- `popRAMValue`
  - moves the RAM value from the RAM slot being pointed at to the pointer value and sets the RAM slot to `0`
- `add`
  - adds the top two stack values together and stores them in the current RAM slot
  - this sets the RAM slot immediately to the right of it to `0` as well
- `a`
  - sets the pointer value and current RAM slot to `97`
- `A`
  - sets the pointer value and the current RAM slot to `65`
- `space`
  - sets the pointer value and the current RAM slot to `32`