package org.makemymanual.manual;

/**
 * An exception specifically relating to the creation of the collated manual.
 */
public class OutputIOException extends ManualException
{
    /**
     * Creates an instance of OutputIOException with an appropriate title.
     */
    public OutputIOException()
    {
        super("Error creating manual!");
    }
}
