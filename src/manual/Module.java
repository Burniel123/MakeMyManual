package manual;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Models a single module in the game, holding its manual page and other module details.
 * May be extended in future to include links to Steam Workshop/timwi's manual site.
 *
 * @author Daniel Burton
 */
public class Module
{
    private String moduleName = null;
    private String moduleCode = null;
    private int difficulty = 0;
    private File manualLocation = null;
    private ArrayList<String> moduleCreators = new ArrayList<String>();
    private LocalDate moduleCreationDate = null;

    /**
     * Creates an instance of Module, requiring its name, list of creators, and a creation date.
     * @param moduleName - the module's display name, eg "Complicated Wires"
     * @param moduleCreators - the module's creator's name(s), eg "Steel Crate Games"
     * @param moduleCreationDate - the module's initial release date, eg 2015-10-08.
     */
    public Module(String moduleName, String moduleCode, int difficulty, String manualPath, ArrayList<String> moduleCreators, LocalDate moduleCreationDate)
    {
        this.moduleName = moduleName;
        this.moduleCode = moduleCode;
        this.difficulty = difficulty;
        this.moduleCreators = moduleCreators;
        this.moduleCreationDate = moduleCreationDate;
        manualLocation = new File(manualPath);
    }

    /**
     * Obtains the location of this module's manual page in the program's index, so that it can be included
     * in a full manual.
     * @return a File object containing the path to this module's manual page.
     */
    public File getManualLocation()
    {
        return manualLocation;
    }

    /**
     * Obtains the name of this module.
     * @return a String corresponding to this module's name.
     */
    public String getModuleName()
    {
        return moduleName;
    }
}
