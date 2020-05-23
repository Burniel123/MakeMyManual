package org.makemymanual.manual;

/**
 * An exception specifically relating to an attempted download of manual page(s) from ktane.timwi.de's source
 * (the community-run repository of manual pages).
 *
 * @author Daniel Burton
 */
public class FileDownloadException extends ManualException
{
    /**
     * Creates an instance of InputIOException, setting an appropriate title.
     */
    public FileDownloadException()
    {
        super("Error reading modules!");
    }
}