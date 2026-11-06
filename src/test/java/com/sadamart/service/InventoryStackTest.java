package com.sadamart.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryStackTest {
    @Test
    void pushAndPopFollowLifoOrder() {
        InventoryStack<String> stack = new InventoryStack<>();
        stack.push("Rice");
        stack.push("Oil");
        stack.push("Tea");

        assertEquals("Tea", stack.peek());
        assertEquals("Tea", stack.pop());
        assertEquals("Oil", stack.pop());
        assertEquals("Rice", stack.pop());
        assertTrue(stack.isEmpty());
    }

    @Test
    void replaceAndDeleteSupportInventoryCrud() {
        InventoryStack<String> stack = new InventoryStack<>();
        stack.push("Rice");
        stack.push("Oil");

        assertTrue(stack.replaceIf("Oil"::equals, "Tea"));
        assertEquals("Tea", stack.peek());
        assertTrue(stack.removeIf("Tea"::equals));
        assertEquals("Rice", stack.peek());
        assertFalse(stack.removeIf("Missing"::equals));
    }

    @Test
    void popAndPeekRejectEmptyStack() {
        InventoryStack<String> stack = new InventoryStack<>();

        assertThrows(IllegalStateException.class, stack::pop);
        assertThrows(IllegalStateException.class, stack::peek);
    }
}
