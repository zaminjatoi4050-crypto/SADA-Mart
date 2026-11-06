package com.sadamart.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * LIFO inventory structure for the Stack assignment.
 *
 * @param <T> inventory item type
 */
public final class InventoryStack<T> {
    private final Deque<T> items = new ArrayDeque<>();

    public void push(T item) {
        items.push(Objects.requireNonNull(item, "Stack item cannot be null"));
    }

    public T pop() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot pop an empty inventory stack");
        }
        return items.pop();
    }

    public T peek() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot peek at an empty inventory stack");
        }
        return items.peek();
    }

    public boolean removeIf(Predicate<? super T> predicate) {
        return items.removeIf(predicate);
    }

    public boolean replaceIf(Predicate<? super T> predicate, T replacement) {
        Objects.requireNonNull(replacement, "Replacement item cannot be null");
        List<T> snapshot = new ArrayList<>(items);
        for (int i = 0; i < snapshot.size(); i++) {
            if (predicate.test(snapshot.get(i))) {
                snapshot.set(i, replacement);
                items.clear();
                for (int j = snapshot.size() - 1; j >= 0; j--) {
                    items.push(snapshot.get(j));
                }
                return true;
            }
        }
        return false;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** Returns items from top to bottom without exposing the stack internals. */
    public List<T> snapshot() {
        return List.copyOf(items);
    }
}
