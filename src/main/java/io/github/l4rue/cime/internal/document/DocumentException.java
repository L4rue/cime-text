/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.

package io.github.l4rue.cime.internal.document;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Checked exception raised while building the in-memory DOM representation.
 *
 * @author dingyh
 */
public class DocumentException extends Exception {

    private Throwable nestedException;

    /**
     * Creates an empty document exception.
     */
    public DocumentException()
    {
        super();
    }

    /**
     * Creates a document exception with a message.
     *
     * @param message failure description
     */
    public DocumentException(String message)
    {
        super(message);
    }

    /**
     * Creates a document exception that wraps another exception.
     *
     * @param nestedException nested cause
     */
    public DocumentException(Throwable nestedException)
    {
        super(nestedException.getMessage());
        this.nestedException = nestedException;
    }

    /**
     * Creates a document exception with both a message and nested cause.
     *
     * @param message failure description
     * @param nestedException nested cause
     */
    public DocumentException(String message, Throwable nestedException)
    {
        super(message);
        this.nestedException = nestedException;
    }

    /**
     * Returns the nested parsing exception, when one exists.
     *
     * @return nested cause or {@code null}
     */
    public Throwable getNestedException()
    {
        return nestedException;
    }

    /**
     * Returns the message including nested exception details when available.
     *
     * @return expanded exception message
     */
    @Override
    public String getMessage()
    {
        if(nestedException != null)
            return super.getMessage() + " Nested exception: " + nestedException.getMessage();
        else
            return super.getMessage();
    }

    /**
     * Prints this exception and its nested cause to the standard error stream.
     */
    @Override
    public void printStackTrace()
    {
        super.printStackTrace();
        if(nestedException != null)
        {
            System.err.print("Nested exception: ");
            nestedException.printStackTrace();
        }
    }

    /**
     * Prints this exception and its nested cause to the supplied stream.
     *
     * @param out destination print stream
     */
    @Override
    public void printStackTrace(PrintStream out)
    {
        super.printStackTrace(out);
        if(nestedException != null)
        {
            out.println("Nested exception: ");
            nestedException.printStackTrace(out);
        }
    }

    /**
     * Prints this exception and its nested cause to the supplied writer.
     *
     * @param writer destination writer
     */
    @Override
    public void printStackTrace(PrintWriter writer)
    {
        super.printStackTrace(writer);
        if(nestedException != null)
        {
            writer.println("Nested exception: ");
            nestedException.printStackTrace(writer);
        }
    }
}

