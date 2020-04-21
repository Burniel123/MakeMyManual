package manual;

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
        setExceptionTitle("Error creating manual!");
    }
/*
    *//**
     * Obtains whether or not the exception occurred after creation of the file (ie during writing).
     * @return true if exception occurred while writing to the file, false if it occurred while creating the file.
     *//*
    public boolean wasCreated()
    {
        return created;
    }*/
}
