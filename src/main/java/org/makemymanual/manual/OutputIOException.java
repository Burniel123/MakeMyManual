package org.makemymanual.manual;

/**
 * An exception specifically relating to the creation of the collated manual.
 */
public class OutputIOException extends ManualException
{
    /**
     * Creates a ManualException with a relevant title for this type of error.
     */
    public OutputIOException()
    {
        super("Error creating manual!");
    }
}
