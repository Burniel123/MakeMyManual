package manual;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
     * Reads a profile.json file to obtain a list of enabled modules.
     * @return an ArrayList of Strings corresponding to module codes which are enabled in the profile.
     * @throws ParseException - if the json file cannot be parsed.
     * @throws IOException - in the event of an IOException.
     * @throws ProfileException - if the parser cannot find an "enabled" list in the profile.
     */
    public ArrayList<String> readJson() throws ParseException, IOException, ProfileException
    {
        JSONObject content = (JSONObject)(new JSONParser().parse(new FileReader(jsonFile)));
        JSONArray array = (JSONArray) content.get("EnabledList");

        if(array == null)
            throw new ProfileException("No enabled list found in profile!");

        ArrayList<String> moduleCodesEnabled = new ArrayList<String>();

        for(Object o : array)
        {//Add the module code for each object in the JSONArray to the list to return.
            System.out.println((String) o);
            moduleCodesEnabled.add((String) o);
        }

        return moduleCodesEnabled;
    }
}
