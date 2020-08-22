package org.makemymanual.manual;

/**
 * An exception specifically relating to an attempted download of manual page(s) from ktane.timwi.de
 * (the community-run repository of manual pages).
 *
 * @author Daniel Burton
 */
public class FileDownloadException extends ManualException
{
    /**
     * Creates a ManualException with a relevant title for this type of error.
     */
    public FileDownloadException()
    {
        super("Error reading modules!");
    }
}