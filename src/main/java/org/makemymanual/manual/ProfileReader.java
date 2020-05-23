package org.makemymanual.manual;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Reads and manages a profile.json file, to be used as module input.
 *
 * @author Daniel Burton
 */
public class ProfileReader
{
    private File jsonFile = null;

    /**
     * Creates a new instance of ProfileReader using a json file to be read.
     * @param jsonFile - a File object for a profile.json.
     */
    public ProfileReader(File jsonFile)
    {
        this.jsonFile = jsonFile;
    }

    /**
     * Read the JSON file assigned to this reader.
     * An external library is not used for this as the functionality required is minimal,
     * and there are no simple libraries with simple enough dependencies to make adding them worthwhile.
     * @return an ArrayList of module codes in the profile's enabled list, each represented by a String.
     * @throws IOException in the event of an IOException when reading the profile.
     * @throws ProfileException if the profile does not feature an enabled list.
     */
    public ArrayList<String> readJson() throws IOException, ProfileException
    {
        BufferedReader reader = new BufferedReader(new FileReader(jsonFile));
        String line = null;
        String innerLine = null;
        ArrayList<String> moduleCodesEnabled = new ArrayList<String>();
        while((line = reader.readLine()) != null)
        {
            if(line.contains("\"EnabledList\": "))
            {
                while(!(innerLine = reader.readLine()).contains("]"))
                {//The presence of a ] indicates the end of this JSON object and hence the end of the enabled list.
                    moduleCodesEnabled.add(innerLine.substring(innerLine.indexOf("\"")+1, innerLine.lastIndexOf("\"")));
                }
                break;
            }
        }

        if(moduleCodesEnabled.isEmpty())
        {//Unable to use the profile unless it has an enabled list, so an exception must be thrown.
            ProfileException pe =  new ProfileException();
            pe.addPossibleCause("No enabled list in your profile.");
            pe.addPossibleCause("Profile is poorly formatted.");
            pe.addPossibleResolution("Download a fresh profile and try again.");
            throw pe;
        }

        return moduleCodesEnabled;
    }
}
