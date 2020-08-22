package org.makemymanual.manual;

/**
 * Custom exception to be thrown when a provided profile does not have an "enabled" list.
 *
 * @author Daniel Burton
 */
public class ProfileException extends ManualException
{
    /**
     * Creates a ManualException with a relevant title for this type of error.
     */
    public ProfileException()
    {
        super("Error reading profile!");
    }
}