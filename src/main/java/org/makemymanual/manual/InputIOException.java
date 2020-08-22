package org.makemymanual.manual;

/**
 * An exception specifically relating to the read of the module list config file.
 *
 * @author Daniel Burton
 */
public class InputIOException extends ManualException
{
    /**
     * Creates a ManualException with a relevant title for this type of error.
     */
    public InputIOException()
    {
        super("Error reading modules!");
    }
}