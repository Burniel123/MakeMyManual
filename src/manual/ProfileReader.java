package manual;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ProfileReader
{
    private File jsonFile = null;

    public ProfileReader(File jsonFile)
    {
        this.jsonFile = jsonFile;
    }

    public ArrayList<String> readJson() throws ParseException, IOException, ProfileException
    {
        JSONObject content = (JSONObject)(new JSONParser().parse(new FileReader(jsonFile)));
        JSONArray array = (JSONArray) content.get("EnabledList");

        if(array == null)
            throw new ProfileException("No enabled list found in profile!");

        ArrayList<String> moduleCodesEnabled = new ArrayList<String>();

        for (Object o : array)
        {
            System.out.println((String) o);
            moduleCodesEnabled.add((String) o);
        }

        return moduleCodesEnabled;
    }

    public static void main(String[] args) throws Exception
    {
        ProfileReader pr = new ProfileReader(new File("test.json"));
        try
        {
            pr.readJson();
        }
        catch(ProfileException e)
        {
            System.out.println(e.getMessage());
        }
    }
}

class ProfileException extends Exception
{
    public ProfileException(String message)
    {
        super(message);
    }
}
