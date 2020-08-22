package org.makemymanual.manual;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Used to read the config TSV files, containing a list of available modules and their info/source,
 * and a mapping of module dependencies.
 *
 * Eventually to be refactored to obtain this file from the repository.
 */
public class ManualListReader
{
    /**
     * Reads a config file and creates Module objects based on these.
     * @return an ArrayList of Module objects, one per line in the TSV config file.
     * @throws InputIOException - in the event of an I/O Error when reading the config file.
     */
    public ArrayList<Module> readModuleList() throws InputIOException
    {
        ArrayList<Module> modules = new ArrayList<Module>();
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("module-config-details.txt")), "ISO-8859-1"));
            String line = null;

            while((line = reader.readLine()) != null)
            {
                String[] detailElements = line.split("\t");
                //Parsing the line into module details:
                String moduleName = removeQuotes(detailElements[0]);
                String moduleCode = removeQuotes(detailElements[1]);
                String difficultyStr = detailElements[2];
                String manualUrl = removeQuotes(detailElements[3]);
                String moduleCreatorsStr = removeQuotes(detailElements[4]);
                String moduleCreationDateStr = removeQuotes(detailElements[5]);
                String categoryStr = detailElements[6];

                if(moduleCode.equals("nonverbalSimon"))
                    moduleName = "\u2756"; //Deals with the awkward nonverbalSimon symbol.

                int difficulty = Integer.parseInt(difficultyStr);
                String[] moduleCreators = moduleCreatorsStr.split(",");
                LocalDate moduleCreationDate = LocalDate.parse(moduleCreationDateStr);
                int category = Integer.parseInt(categoryStr);
                Module module = new Module(moduleName, moduleCode, difficulty,
                        manualUrl, moduleCreators, moduleCreationDate, category);

                modules.add(module);
            }
        }
        catch(Exception e)
        {//Throw with false if some kind of input exception.
            InputIOException iioe = new InputIOException();
            iioe.addPossibleCause("Module config file has been incorrectly moved or edited.");
            iioe.addPossibleResolution("Reinstall the application.");
            iioe.addPossibleResolution("Try again later.");
            throw iioe;
        }
        return modules;
    }

    /**
     * Reads a dependency config file and creates a mapping from modules to other modules whose manual pages these
     * modules also require.
     * @return a HashMap mapping module codes to a list of module codes which the module relies on.
     * @throws InputIOException - in the event of an I/O Error when reading the dependency file.
     */
    public HashMap<String, String[]> readDependenciesList() throws InputIOException
    {
        HashMap<String, String[]> dependencies = new HashMap<String, String[]>();
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("module-dependencies.txt")), "ISO-8859-1"));
            String line = null;

            while((line = reader.readLine()) != null)
            {
                String[] depList = line.split("\t");
                String mainCode = depList[0];
                String[] deps = (line.substring(line.indexOf("\t") + 1).split("\t"));
                dependencies.put(mainCode, deps);
            }
        }
        catch(Exception e)
        {//Throw with false if some kind of input exception.
            InputIOException iioe = new InputIOException();
            iioe.addPossibleCause("Module dependencies file has been incorrectly moved or edited.");
            iioe.addPossibleResolution("Reinstall the application.");
            iioe.addPossibleResolution("Try again later.");
            throw iioe;
        }
        return dependencies;
    }

    /**
     * Removes the quotation marks from strings which have had them applied by conversion to TSV.
     * @param phrase - the phrase to check for quotation marks.
     * @return the input phrase without surrounding quotes.
     */
    private String removeQuotes(String phrase)
    {
        if(phrase.charAt(0) == '"' && phrase.charAt(phrase.length() - 1) == '"')
            return phrase.substring(1, phrase.length()-1);
        else
            return phrase;
    }
}
