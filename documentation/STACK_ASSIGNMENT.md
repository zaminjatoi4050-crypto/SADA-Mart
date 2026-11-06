# Stack-Based Inventory Management

## Selected system

SADA Mart is the inventory management system. The inventory is implemented with a
Stack, as assigned for Group B. A stack follows **LIFO** (Last In, First Out):
the newest product pushed into inventory is the first product removed.

## Implementation

`com.sadamart.service.InventoryStack<T>` is the data structure used by the
application. It provides:

- `push(item)`: adds a product to the top of the stack.
- `pop()`: removes and returns the top product.
- `peek()`: reads the top product without removing it.
- `replaceIf(...)`: updates a product while preserving its stack position.
- `removeIf(...)`: deletes a matching product.
- `snapshot()`: reads the stack from top to bottom for the inventory screen.

The Mart UI uses the stack as the source of truth. Adding a product pushes it
onto the stack. “Pop Top Product” removes the newest product. “Update Selected
Product” updates the top product, and “Delete Selected Product” removes it using
`pop()`. The Products screen displays the stack from top to bottom.

## CRUD demonstration

1. **Create:** Products > Add Product. The new product is pushed to the top.
2. **Read:** Products shows the stack contents from top to bottom; the top item
   is the next item that `pop()` will remove.
3. **Update:** Products > Update Selected Product changes the top product's
   selling price without changing its stack position.
4. **Delete:** Products > Delete Selected Product removes the top product with
   `pop()`. Products > Pop Top Product provides the same explicit Stack action.

## Example LIFO output

```text
push(Rice) -> [Rice]
push(Oil)  -> [Oil, Rice]
push(Tea)  -> [Tea, Oil, Rice]
peek()     -> Tea
pop()      -> Tea
pop()      -> Oil
```

## Verification

`InventoryStackTest` verifies push/pop LIFO order, peek, update, delete, empty
stack behavior, and unsuccessful deletion. Run `mvnw.cmd test` on Windows.

## Remaining submission items

The source project can provide the implementation and test evidence. A GitHub
repository URL, a Word report upload, and a Google Drive video link must be
created by the student and attached to the submitted report because they require
the student's accounts and presentation recording.
