package manual;

import java.util.ArrayList;

public abstract class ManualException extends Exception
{
    private String exceptionTitle = null;
    private ArrayList<String> possibleCauses = new ArrayList<String>();
    private ArrayList<String> possibleResolutions = new ArrayList<String>();

    public String getExceptionTitle()
    {
        return exceptionTitle;
    }

    public ArrayList<String> getPossibleCauses()
    {
        return possibleCauses;
    }

    public ArrayList<String> getPossibleResolutions()
    {
        return possibleResolutions;
    }

    protected void setExceptionTitle(String exceptionTitle)
    {
        this.exceptionTitle = exceptionTitle;
    }

    public void addPossibleCause(String possibleCause)
    {
        possibleCauses.add(possibleCause);
    }

    public void addPossibleResolution(String possibleResolution)
    {
        possibleResolutions.add(possibleResolution);
    }
}
