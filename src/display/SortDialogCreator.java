package display;

import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import manual.Module;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Holds functionality relating to the sort dialog, and the sorting and filtering of the modules.
 */
public class SortDialogCreator
{
    /**
     * Launch a Dialog to handle sorting and filtering mods on the stage.
     */
    void applyModuleSort()
    {
        //Setting up containers for the dialog:
        Dialog<Void> sortAndFilterDialog = new Dialog<Void>();
        DialogPane sfDialogPane = new DialogPane();
        GridPane dialogGrid = new GridPane();
        dialogGrid.setHgap(2);
        dialogGrid.setVgap(2);
        sfDialogPane.setContent(dialogGrid);
        sortAndFilterDialog.setDialogPane(sfDialogPane);
        sortAndFilterDialog.setTitle("Sort/Filter Modules");
        sortAndFilterDialog.setHeaderText("Sort & Filter Modules");
        //Setting up buttons for sorting:
        ToggleGroup sort = new ToggleGroup();
        RadioButton sortByName = new RadioButton("Module Name");
        sortByName.setToggleGroup(sort);
        sortByName.setSelected(true);
        dialogGrid.add(sortByName,0,2);
        RadioButton sortByCode = new RadioButton("In-Game Module Code");
        sortByCode.setToggleGroup(sort);
        dialogGrid.add(sortByCode,0,3);
        RadioButton sortByDiff = new RadioButton("Expert Difficulty");
        sortByDiff.setToggleGroup(sort);
        dialogGrid.add(sortByDiff,0,4);
        RadioButton sortByCreator = new RadioButton("Module Creator");
        sortByCreator.setToggleGroup(sort);
        dialogGrid.add(sortByCreator,0,5);
        RadioButton sortByDate = new RadioButton("Module Publishing Date");
        sortByDate.setToggleGroup(sort);
        dialogGrid.add(sortByDate,0,6);
        CheckBox reverse = new CheckBox("Reverse Order");
        dialogGrid.add(reverse, 0, 7);
        //Setting up buttons for filtering:
        CheckBox allowVanilla = new CheckBox("Regular Modules: Vanilla");
        dialogGrid.add(allowVanilla, 2, 2);
        CheckBox allowRegular = new CheckBox("Regular Modules: Mods");
        dialogGrid.add(allowRegular, 2, 3);
        CheckBox allowNeedy = new CheckBox("Needy Modules");
        dialogGrid.add(allowNeedy, 2, 4);
        CheckBox allowAppendices = new CheckBox("Appendices");
        dialogGrid.add(allowAppendices, 2, 5);
        //Miscellaneous components for the dialog:
        Separator separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);
        dialogGrid.add(separator, 1, 0, 1, 7);
        Label sortTitle = new Label("Sort Modules By:");
        sortTitle.setStyle("-fx-font-weight: bold");
        dialogGrid.add(sortTitle, 0, 0);
        Label filterTitle = new Label("Include Module Types:");
        filterTitle.setStyle("-fx-font-weight: bold");
        dialogGrid.add(filterTitle, 2, 0);
        //Add buttons to the sort dialog:
        sortAndFilterDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        sortAndFilterDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        sortAndFilterDialog.setResultConverter(new Callback<ButtonType, Void>()
        {
            @Override
            public Void call(ButtonType buttonType)
            {//Defines what to do when the dialog is submitted.
                if(buttonType == ButtonType.OK)
                {
                    Main.MODULES_DISPLAYED = new ArrayList<Module>(Main.MODULES_AVAILABLE);

                    //Deal with sorting the list first:
                    String sortByStr = ((RadioButton)sort.getSelectedToggle()).getText();
                    boolean reverseOrder = false;
                    if(reverse.isSelected())
                        reverseOrder = true;
                    if(sortByStr.equals("Module Name"))
                        sortModules(0, reverseOrder);
                    else if(sortByStr.equals("In-Game Module Code"))
                        sortModules(1, reverseOrder);
                    else if(sortByStr.equals("Expert Difficulty"))
                        sortModules(2, reverseOrder);
                    else if(sortByStr.equals("Module Creator"))
                        sortModules(3, reverseOrder);
                    else
                        sortModules(4, reverseOrder);

                    //Now deal with filtering the sorted list, as filtering is stable:
                    ArrayList<Integer> categories = new ArrayList<Integer>();
                    if(allowVanilla.isSelected())
                        categories.add(0);
                    if(allowRegular.isSelected())
                        categories.add(1);
                    if(allowNeedy.isSelected())
                        categories.add(2);
                    if(allowAppendices.isSelected())
                        categories.add(3);
                    filterModules(categories);
                }
                return null;
            }
        });
        sortAndFilterDialog.showAndWait();
    }

    /**
     * Sorts the modules according to a certain property.
     * @param sortBy - an integer from 0 to 4:
     *               0 - sort by module name.
     *               1 - sort by module code.
     *               2 - sort by difficulty.
     *               3 - sort by first creator's name.
     *               4 - sort by date created.
     */
    void sortModules(int sortBy, boolean reverse)
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
                        if(o1.getModuleName().compareToIgnoreCase(o2.getModuleName()) > 0)
                            toReturn = 1;
                        else if(o1.getModuleName().compareToIgnoreCase(o2.getModuleName()) == 0)
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
     * Applies a filter based on module categories and then renders this.
     * @param include
     */
    private void filterModules(ArrayList<Integer> include)
    {
        ArrayList<Module> temp = new ArrayList<Module>(Main.MODULES_DISPLAYED);
        for(Module module : temp)
        {
            if(!include.contains(module.getCategory()))
                Main.MODULES_DISPLAYED.remove(module);
        }
        Main.searchModules(((TextField)((HBox)Main.ROOT_PANE.getChildren().get(0)).getChildren().
                get(0)).getText());
    }
}
