/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   ElementStack.java

package io.github.l4rue.cime.internal.util;

import org.w3c.dom.Element;

/**
 * Lightweight stack implementation for DOM elements during parsing.
 *
 * @author dingyh
 */
public class ElementStack {

    private static final int DEFAULT_CAPACITY = 50;

    protected Element[] stack;
    protected int lastElementIndex;

    /**
     * Creates a stack with the default capacity.
     */
    public ElementStack() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates a stack with the requested initial capacity.
     *
     * @param defaultCapacity initial array capacity
     */
    public ElementStack(int defaultCapacity) {
        lastElementIndex = -1;
        stack = new Element[defaultCapacity];
    }

    /**
     * Removes all elements from the stack.
     */
    public void clear() {
        lastElementIndex = -1;
    }

    /**
     * Returns the current top element without removing it.
     *
     * @return current top element, or {@code null} when the stack is empty
     */
    public Element peekElement() {
        if (lastElementIndex < 0) {
            return null;
        } else {
            return stack[lastElementIndex];
        }
    }

    /**
     * Removes and returns the current top element.
     *
     * @return removed top element, or {@code null} when the stack is empty
     */
    public Element popElement() {
        if (lastElementIndex < 0) {
            return null;
        } else {
            return stack[lastElementIndex--];
        }
    }

    /**
     * Pushes an element onto the stack.
     *
     * @param element element to push
     */
    public void pushElement(Element element) {
        int length = stack.length;
        if (++lastElementIndex >= length) {
            reallocate(length * 2);
        }
        stack[lastElementIndex] = element;
    }

    protected void reallocate(int size) {
        Element[] oldStack = stack;
        stack = new Element[size];
        System.arraycopy(oldStack, 0, stack, 0, oldStack.length);
    }

    /**
     * Returns the number of stored elements.
     *
     * @return current stack size
     */
    public int size() {
        return lastElementIndex + 1;
    }

    /**
     * Returns the element at the supplied depth.
     *
     * @param depth zero-based depth in the internal stack array
     * @return element at the requested depth, or {@code null} when out of bounds
     */
    public Element getElement(int depth) {
        Element element;
        try {
            element = stack[depth];
        } catch (ArrayIndexOutOfBoundsException e) {
            element = null;
        }
        return element;
    }

    /**
     * Returns the current top element.
     *
     * @return current top element, or {@code null} when the stack is empty
     */
    public Element getCurrent() {
        return peekElement();
    }
}

 
