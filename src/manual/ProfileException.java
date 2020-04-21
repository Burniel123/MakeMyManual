package manual;

/**
 * Custom exception to be thrown when a provided profile does not have an "enabled" list.
 *
 * @author Daniel Burton
 */
public class ProfileException extends ManualException
{

    /**
     * Creates a ProfileException to be thrown with appropriate title.
     */
    public ProfileException()
    {
        setExceptionTitle("Error reading profile!");
    }
}