package manual;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class ManualListReader
{
    private static final File SUPPORTED_MODULE_FILE = new File("./resources/modules-config-details.txt");
    private static final ArrayList<java.lang.Module> MODULES_AVAILABLE = new ArrayList<java.lang.Module>();

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

                String moduleName = detailElements[0];
                String moduleCode = detailElements[1];
                String difficultyStr = detailElements[2];
                String manualPath = detailElements[3];
                String moduleCreatorsStr = detailElements[4];
                String moduleCreationDateStr = detailElements[5];
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
}
