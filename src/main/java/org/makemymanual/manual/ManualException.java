package org.makemymanual.manual;

import java.util.ArrayList;

/**
 * Parent class for any exception specific to this application, as opposed to exceptions thrown by
 * standard processes (eg a simple ParseException).
 *
 * @author Daniel Burton
 */
public abstract class ManualException extends Exception
{
    private String exceptionTitle = null;
    private final ArrayList<String> possibleCauses = new ArrayList<String>();
    private final ArrayList<String> possibleResolutions = new ArrayList<String>();

    /**
     * Constructor used to create a new instance of a subclass of ManualException.
     * @param exceptionTitle - the title to be given to this Exception.
     */
    protected ManualException(String exceptionTitle)
    {
        this.exceptionTitle = exceptionTitle;
    }

    /**
     * Obtains the title assigned to this exception (appears as the title and header in alerts).
     * @return this Exception's title.
     */
    public String getExceptionTitle()
    {
        return exceptionTitle;
    }

    /**
     * Obtains all possible causes for this exception being thrown.
     * @return ArrayList of Strings describing possible causes of this exception.
     */
    public ArrayList<String> getPossibleCauses()
    {
        return possibleCauses;
    }

    /**
     * Obtains all possible resolutions to correct this exception.
     * @return ArrayList of Strings describing possible resolutions for this exception.
     */
    public ArrayList<String> getPossibleResolutions()
    {
        return possibleResolutions;
    }

    /**
     * Adds a cause to the list of possible causes for this exception being thrown.
     * @param possibleCause - a String detailing a possible reason for this exception.
     */
    public void addPossibleCause(String possibleCause)
    {
        possibleCauses.add(possibleCause);
    }

    /**
     * Adds a resolution to the list of possible resolutions for this exception.
     * @param possibleResolution - a String detailing a possible correction to resolve this exception.
     */
    public void addPossibleResolution(String possibleResolution)
    {
        possibleResolutions.add(possibleResolution);
    }
}
