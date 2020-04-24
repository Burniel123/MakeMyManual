package display;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import manual.Module;

import java.util.ArrayList;

/**
 * Holds functionality relating to the sort/filter dialog, to pop up as requested.
 */
public class SortDialog extends Dialog<Void> implements Sortable
{
    private CheckBox allowVanilla = null;
    private CheckBox allowRegular = null;
    private CheckBox allowNeedy = null;
    private CheckBox allowAppendices = null;
    private CheckBox reverse = null;
    private ToggleGroup sort = null;

    /**
     * Creates a new SortDialog.
     */
    SortDialog()
    {
        //Setting up containers for the dialog:
        DialogPane sfDialogPane = new DialogPane();
        sfDialogPane.getStylesheets().add(getClass().getResource("dialogStyle.css").toExternalForm());
        sfDialogPane.getStyleClass().add("dialogStyle");
        GridPane dialogGrid = new GridPane();
        dialogGrid.setHgap(2);
        dialogGrid.setVgap(2);
        sfDialogPane.setContent(dialogGrid);
        setDialogPane(sfDialogPane);
        setTitle("Sort/Filter Modules");
        setHeaderText("Sort & Filter Modules");

        dialogGrid.getColumnConstraints().add(new ColumnConstraints(160));

        //Setting up buttons for sorting:
        sort = new ToggleGroup();
        RadioButton sortByName = new RadioButton("Module Name");
        sortByName.setToggleGroup(sort);
        sortByName.setSelected(true);
        sortByName.setWrapText(true);
        StackPane namePane = new StackPane(sortByName);
        namePane.setAlignment(Pos.CENTER_LEFT);
        dialogGrid.add(namePane,0,2);
        RadioButton sortByCode = new RadioButton("In-Game Module Code");
        sortByCode.setToggleGroup(sort);
        sortByCode.setWrapText(true);
        StackPane codePane = new StackPane(sortByCode);
        codePane.setAlignment(Pos.CENTER_LEFT);
        dialogGrid.add(codePane,0,3);
        RadioButton sortByDiff = new RadioButton("Expert Difficulty");
        sortByDiff.setToggleGroup(sort);
        sortByDiff.setWrapText(true);
        StackPane diffPane = new StackPane(sortByDiff);
        diffPane.setAlignment(Pos.CENTER_LEFT);
        dialogGrid.add(diffPane,0,4);
        RadioButton sortByCreator = new RadioButton("Module Creator");
        sortByCreator.setToggleGroup(sort);
        sortByCreator.setWrapText(true);
        StackPane creaPane = new StackPane(sortByCreator);
        creaPane.setAlignment(Pos.CENTER_LEFT);
        dialogGrid.add(creaPane,0,5);
        RadioButton sortByDate = new RadioButton("Module Publishing Date");
        sortByDate.setToggleGroup(sort);
        sortByDate.setWrapText(true);
        StackPane datePane = new StackPane(sortByDate);
        datePane.setAlignment(Pos.CENTER_LEFT);
        dialogGrid.add(datePane,0,6);
        reverse = new CheckBox("Reverse Order");
        dialogGrid.add(reverse, 0, 7);

        //Setting up buttons for filtering:
        allowVanilla = new CheckBox("Regular Modules: Vanilla");
        allowVanilla.setWrapText(true);
        StackPane vanPane = new StackPane(allowVanilla);
        vanPane.setAlignment(Pos.CENTER_LEFT);
        dialogGrid.add(vanPane, 2, 2);
        allowRegular = new CheckBox("Regular Modules: Mods");
        allowRegular.setWrapText(true);
        StackPane regPane = new StackPane(allowRegular);
        regPane.setAlignment(Pos.CENTER_LEFT);
        dialogGrid.add(regPane, 2, 3);
        allowNeedy = new CheckBox("Needy Modules");
        allowNeedy.setWrapText(true);
        StackPane needPane = new StackPane(allowNeedy);
        needPane.setAlignment(Pos.CENTER_LEFT);
        dialogGrid.add(needPane, 2, 4);
        allowAppendices = new CheckBox("Appendices");
        allowAppendices.setWrapText(true);
        StackPane appPane = new StackPane(allowAppendices);
        appPane.setAlignment(Pos.CENTER_LEFT);
        dialogGrid.add(appPane, 2, 5);
        dialogGrid.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(160));
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
        //sfDialogPane.setMinHeight(Region.USE_PREF_SIZE);

        //Add buttons to the sort dialog:
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

    }

    /**
     * Adds behaviour for when the dialog is submitted, including carrying out the relevant
     * sorts and filters.
     */
    void setResultConversion()
    {
        setResultConverter(new Callback<ButtonType, Void>()
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
    }
}