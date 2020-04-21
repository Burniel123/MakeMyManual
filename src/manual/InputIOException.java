package manual;

/**
 * An exception specifically relating to the read of the module list config file.
 *
 * @author Daniel Burton
 */
public class InputIOException extends ManualException
{
    /**
     * Creates an instance of InputIOException, setting an appropriate title.
     */
    public InputIOException()
    {
        super("Error reading modules!");
    }
}