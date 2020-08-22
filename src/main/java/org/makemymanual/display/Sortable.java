package org.makemymanual.display;

import org.makemymanual.manual.Module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Holds sorting and filtering methods.
 *
 * @author Daniel Burton
 */
public interface Sortable
{
    /**
     * Sorts the modules according to a certain property.
     * @param sortBy - an integer from 0 to 4:
     *               0 - sort by module name.
     *               1 - sort by module code.
     *               2 - sort by difficulty.
     *               3 - sort by first creator's name.
     *               4 - sort by date created.
     */
    default void sortModules(int sortBy, boolean reverse)
    {
        Collections.sort(Main.MODULES_DISPLAYED, new Comparator<Module>()
        {
            @Override
            public int compare(Module o1, Module o2)
            {
                int toReturn = 0;
                switch (sortBy)
                {//Implement a different way of comparing objects depending on the required field.
                    case 0 :
                        if(removeThe(o1.getModuleName()).compareToIgnoreCase(removeThe(o2.getModuleName())) > 0)
                            toReturn = 1;
                        else if(removeThe(o1.getModuleName()).compareToIgnoreCase(removeThe(o2.getModuleName())) == 0)
                            toReturn = 0;
                        else
                            toReturn = -1;break;
                    case 1 :
                        if(o1.getModuleCode().compareToIgnoreCase(o2.getModuleCode()) > 0)
                            toReturn = 1;
                        else if(o1.getModuleCode().compareToIgnoreCase(o2.getModuleCode()) == 0)
                            toReturn = 0;
                        else
                            toReturn = -1;break;
                    case 2 :
                        if(o1.getDifficulty() > o2.getDifficulty())
                            toReturn = 1;
                        else if(o1.getDifficulty() == o2.getDifficulty())
                            toReturn = 0;
                        else
                            toReturn = -1;break;
                    case 3 :
                        if(o1.getModuleCreators()[0].compareToIgnoreCase(o2.getModuleCreators()[0]) > 0)
                            toReturn = 1;
                        else if(o1.getModuleCreators()[0].compareToIgnoreCase(o2.getModuleCreators()[0]) == 0)
                            toReturn = 0;
                        else
                            toReturn = -1;break;
                    case 4 :
                        if(o1.getModuleCreationDate().compareTo(o2.getModuleCreationDate()) > 0)
                            toReturn = 1;
                        else if(o1.getModuleCreationDate().compareTo(o2.getModuleCreationDate()) == 0)
                            toReturn = 0;
                        else
                            toReturn = -1;break;
                    default : toReturn = 0;
                }
                if(reverse)//If the user wants the list reversed, flip the comparison result.
                    toReturn = -toReturn;
                return toReturn;
            }
        });
    }

    /**
     * Removes the word "the" from any module names so the word can be ignored for sorting.
     * @param toRemove - the module name to remove "the" from.
     * @return a String with any starting "the" removed.
     */
    default String removeThe(String toRemove)
    {
        if(toRemove.startsWith("The "))
        {
            return toRemove.substring(4);
        }
        return toRemove;
    }

    /**
     * Applies a filter based on module categories and then renders this.
     * @param include - ArrayList of categories to include.
     */
    default void filterModules(ArrayList<Integer> include)
    {
        ArrayList<Module> temp = new ArrayList<Module>(Main.MODULES_DISPLAYED);
        for(Module module : temp)
        {
            if(!include.contains(module.getCategory()))
                Main.MODULES_DISPLAYED.remove(module);
        }
        Main.searchModules(Main.ROOT_PANE.getSearchBarContents());
    }
}
