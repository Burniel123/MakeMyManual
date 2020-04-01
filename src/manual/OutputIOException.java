package manual;

import java.io.IOException;

/**
 * Wraps IOException to create an exception specifically relating to the creation of the collated manual.
 */
public class OutputIOException extends IOException
{
    /**
     * Creates an instance of OutputIOException, requiring an error to detail the problem.
     * @param fileName - name of the file which caused the exception to be thrown.
     * @param err - Throwable detailing the problem which has caused the exception to be thrown.
     */
    public OutputIOException(String fileName, Throwable err)
    {
        super(fileName, err);
    }
}
