package manual;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Used to read the config TSV file, containing a list of available modules and their info/source.
 */
public class ManualListReader
{
    private static final File SUPPORTED_MODULE_FILE = new File("./resources/modules-config-details.txt");
    private static final ArrayList<java.lang.Module> MODULES_AVAILABLE = new ArrayList<java.lang.Module>();

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
            BufferedReader reader = new BufferedReader(new FileReader(SUPPORTED_MODULE_FILE));
            String line = null;

            while((line = reader.readLine()) != null)
            {
                String[] detailElements = line.split("\t");
                //Parsing the line into module details:
                String moduleName = removeQuotes(detailElements[0]);
                String moduleCode = removeQuotes(detailElements[1]);
                String difficultyStr = detailElements[2];
                String manualPath = removeQuotes(detailElements[3]);
                String moduleCreatorsStr = removeQuotes(detailElements[4]);
                String moduleCreationDateStr = removeQuotes(detailElements[5]);
                String categoryStr = detailElements[6];

                int difficulty = Integer.parseInt(difficultyStr);
                String[] moduleCreators = moduleCreatorsStr.split(",");
                LocalDate moduleCreationDate = LocalDate.parse(moduleCreationDateStr);
                int category = Integer.parseInt(categoryStr);
                Module module = new Module(moduleName, moduleCode, difficulty,
                        manualPath, moduleCreators, moduleCreationDate, category);

                modules.add(module);
            }
        }
        catch(IOException e)
        {//Throw with false if some kind of input exception.
            throw new InputIOException(SUPPORTED_MODULE_FILE.getPath(), e, false);
        }
        catch(Exception e)
        {//Throw with true if some kind of formatting or miscellaneous exception.
            throw new InputIOException(SUPPORTED_MODULE_FILE.getPath(), e, true);
        }
        return modules;
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
