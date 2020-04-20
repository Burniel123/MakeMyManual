package manual;

/**
 * Custom exception to be thrown when a provided profile does not have an "enabled" list.
 *
 * @author Daniel Burton
 */
public class ProfileException extends Exception
{

    /**
     * Creates a ProfileException to be thrown, with an error message.
     * @param message - the message associated with the exception.
     */
    public ProfileException(String message)
    {
        super(message);
    }
}