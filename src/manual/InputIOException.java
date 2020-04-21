package manual;

/**
 * An exception specifically relating to the read of the module list config file.
 *
 * @author Daniel Burton
 */
public class InputIOException extends ManualException
{
    private boolean formattingRelated = false;

    /**
     * Creates an instance of InputIOException, requiring an error to detail the problem.
     * @param fileName - name of the file which caused the exception to be thrown.
     * @param err - Throwable detailing the problem which has caused the exception to be thrown.
     * @param formattingRelated - whether or not the file had already successfully been read when the exception was thrown.
     */
    public InputIOException()
    {
        setExceptionTitle("Error reading modules!");
    }

/*    *//**
     * Obtains whether or not the error is to do with the data formatting within the file (as opposed to reading the text).
     * @return true if the exception occurred due to data formatting, false otherwise.
     *//*
    public boolean isFormattingRelated()
    {
        return formattingRelated;
    }*/
}