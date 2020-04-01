package manual;

import java.io.IOException;

/**
 * Wraps IOException to create an exception specifically relating to the creation of the collated manual.
 */
public class OutputIOException extends IOException
{
    private boolean created = false;
    /**
     * Creates an instance of OutputIOException, requiring an error to detail the problem.
     * @param fileName - name of the file which caused the exception to be thrown.
     * @param err - Throwable detailing the problem which has caused the exception to be thrown.
     */
    public OutputIOException(String fileName, Throwable err, boolean created)
    {
        super(fileName, err);
        this.created = created;
    }

    /**
     * Obtains whether or not the exception occurred after creation of the file (ie during writing).
     * @return true if exception occurred while writing to the file, false if it occurred while creating the file.
     */
    public boolean wasCreated()
    {
        return created;
    }
}
