package manual;

import java.io.File;
import java.util.ArrayList;

/**
 * Manages the content of, and eventually creates, a collated manual for the user's desired modules.
 *
 * @author Daniel Burton
 */
public class ManualCreator
{
    private File outputTexFile = null;
    private ArrayList<Module> modules = new ArrayList<Module>();

    /**
     * Creates an instance of ManualCreator, requiring the path it will write to.
     * TODO: make sure this only ever contains a tex file!
     * @param texFilePath - path to a .tex file to write manual pages to.
     */
    public ManualCreator(String texFilePath)
    {
        outputTexFile = new File(texFilePath);
    }

    /**
     * Adds a module to the list of modules to be included in this manual.
     * @param module - a Module object to add to the manual.
     */
    public void addModule(Module module)
    {
        modules.add(module);
    }

    /**
     * Clears the list of modules,
     */
    public void clearModules()
    {
        modules.clear();
    }


}
