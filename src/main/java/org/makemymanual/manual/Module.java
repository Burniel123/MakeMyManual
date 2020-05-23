package org.makemymanual.manual;

import java.time.LocalDate;

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
    private String manualLocation = null;
    private String[] moduleCreators = null;
    private LocalDate moduleCreationDate = null;
    private int category = 0;
    private boolean active = false;

    /**
     * Creates an instance of Module, requiring its name, list of creators, and a creation date.
     * @param moduleName - the module's display name, eg "Complicated Wires"
     * @param moduleCreators - the module's creator's name(s), eg "Steel Crate Games"
     * @param moduleCreationDate - the module's initial release date, eg 2015-10-08.
     */
    public Module(String moduleName, String moduleCode, int difficulty, String manualLocation,
                  String[] moduleCreators, LocalDate moduleCreationDate, int category)
    {
        this.moduleName = moduleName;
        this.moduleCode = moduleCode;
        this.difficulty = difficulty;
        this.moduleCreators = moduleCreators;
        this.moduleCreationDate = moduleCreationDate;
        this.category = category;
        this.manualLocation = manualLocation;
    }

    /**
     * Obtains the location of this module's manual page on the KTANE manual repo as a String.
     * @return a String containing the path to this module's manual page.
     */
    public String getManualLocation()
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

    /**
     * Obtains the KTANE module code of this module.
     * @return a String equal to the module code for this module, as used in the game.
     */
    public String getModuleCode()
    {
        return moduleCode;
    }

    /**
     * Obtains the difficulty of this module, as defined by Timwi's manual repository.
     * @return an integer corresponding to the difficulty (for the expert) of the module:
     *              1 - Very Easy (eg "Wires")
     *              2 - Easy (eg "Memory")
     *              3 - Medium (eg "Adventure Game")
     *              4 - Hard (eg "Forget Me Not")
     *              5 - Very Hard (eg "Mastermind Cruel")
     */
    public int getDifficulty()
    {
        return difficulty;
    }

    /**
     * Obtains the list of creators for this module.
     * @return a String array of usernames of the module's creators.
     */
    public String[] getModuleCreators()
    {
        return moduleCreators;
    }

    /**
     * Obtains the date this module was first published.
     * @return a LocalDate containing the module's publishing date.
     */
    public LocalDate getModuleCreationDate()
    {
        return moduleCreationDate;
    }

    /**
     * Set this Module as currently active/selected.
     */
    public void activate()
    {
        active = true;
    }

    /**
     * Set this Module as currently inactive/deselected.
     */
    public void deactivate()
    {
        active = false;
    }

    /**
     * Obtains whether or not this Module is currently active/selected.
     * @return true if the module is selected, false otherwise.
     */
    public boolean isActive()
    {
        return active;
    }

    /**
     * Obtains the category of this Module.
     * @return an integer from 0 to 3 as follows:
     *              0 - Vanilla Regular
     *              1 - Modded Regular
     *              2 - Needy
     *              3 - Appendix
     */
    public int getCategory()
    {
        return category;
    }
}