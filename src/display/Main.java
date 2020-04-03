package display;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import manual.InputIOException;
import manual.ManualListReader;
import manual.Module;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

public class Main extends Application
{
    private static final VBox ROOT_PANE = new VBox(3);
    private static final String DEFAULT_MODULE_STYLE = "-fx-background-color: #f25d55";
    private static final String SELECTED_MODULE_STYLE = "-fx-background-color: #669900";
    private static final String DEFAULT_BACK_STYLE = "-fx-background-color: #e5cb90";
    private static boolean exceptionOnBoot = false;

    private ArrayList<Module> MODULES_AVAILABLE = new ArrayList<Module>();

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        //Setting up an area for the modules:
        final FlowPane modulesPane = new FlowPane(2,2);
        final ScrollPane scrollableWindow = new ScrollPane(modulesPane);
        scrollableWindow.setFitToHeight(true);
        scrollableWindow.setFitToWidth(true);
        modulesPane.setStyle(DEFAULT_BACK_STYLE);
        //Setting up components for the top menu bar:
        final HBox topMenu = new HBox(2);
        topMenu.setPadding(new Insets(5));
        final TextField searchBarInput = new TextField("Search for modules...");
        searchBarInput.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchBarInput, Priority.ALWAYS);
        final Button searchBarSubmit = new Button("Search!");
        final Separator searchSeparator = new Separator();
        searchSeparator.setOrientation(Orientation.VERTICAL);
        final Button sortMenu = new Button("Sort");
        final Button presetsButton = new Button("Presets");
        final Button makeIt = new Button("Make My Manual!");
        topMenu.getChildren().addAll(searchBarInput, searchBarSubmit, searchSeparator,
                sortMenu, presetsButton, makeIt);
        //Setting up components for the "num selected" bar.
        final HBox numSelectedBar = new HBox(2);
        final Label numSelected = new Label("Modules Selected: 0/98");
        numSelectedBar.getChildren().add(numSelected);
        ROOT_PANE.getChildren().addAll(topMenu, numSelectedBar);
        ROOT_PANE.getChildren().add(scrollableWindow);
        renderModules();

        sortMenu.setOnMouseClicked(e -> applyModuleSort());

        final Scene scene = new Scene(ROOT_PANE);
        primaryStage.setTitle("MakeMyManual");
        primaryStage.setScene(scene);
        primaryStage.setWidth(750);
        primaryStage.setHeight(500);

        if(exceptionOnBoot)
        {
            Alert exceptionAlert = new Alert(Alert.AlertType.ERROR);
            exceptionAlert.setTitle("Error loading configuration files!");
            exceptionAlert.setHeaderText("Error encountered while loading modules.\n" +
                    "Has it been edited or removed?");
            exceptionAlert.setContentText("Please try rebooting the application.\n" +
                    "If problem persists, please contact Daniel Burton.");
            exceptionAlert.showAndWait();
            Platform.exit();
        }
        else
            primaryStage.show();
    }

    /**
     * Launch a Dialog to handle sorting and filtering mods on the stage.
     */
    private void applyModuleSort()
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

        sortAndFilterDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        sortAndFilterDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        sortAndFilterDialog.setResultConverter(new Callback<ButtonType, Void>()
        {
            @Override
            public Void call(ButtonType buttonType)
            {//Defines what to do when the dialog is submitted.
                if(buttonType == ButtonType.OK)
                {
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
     * Clear all modules off the screen, generally so they can be re-rendered.
     */
    private void clearModules()
    {
        ScrollPane scroll = (ScrollPane)ROOT_PANE.getChildren().get(2);
        ((FlowPane)scroll.getContent()).getChildren().clear();
    }

    /**
     * Renders module regions to the stage.
     */
    private void renderModules()
    {
        for(int i = 0; i < MODULES_AVAILABLE.size(); i++)
        {
            //Components to build each module region:
            Module module = MODULES_AVAILABLE.get(i);
            final StackPane modulePane = new StackPane();
            final Region moduleRegion = new Region();
            final Label moduleName = new Label(module.getModuleName() + "\n\n");
            moduleName.setPadding(new Insets(2));
            modulePane.setAlignment(Pos.BOTTOM_CENTER);
            final Label moduleCode = new Label(module.getModuleCode());
            moduleCode.setFont(new Font(7));
            if(module.isActive())
                moduleRegion.setStyle(SELECTED_MODULE_STYLE);
            else
                moduleRegion.setStyle(DEFAULT_MODULE_STYLE);
            moduleRegion.setMinSize(100, 20);
            //Event handling code for each module:
            modulePane.setOnMouseClicked(e ->
            {
                if(moduleRegion.getStyle().equals(DEFAULT_MODULE_STYLE))
                {
                    moduleRegion.setStyle(SELECTED_MODULE_STYLE);
                    module.activate();
                }
                else
                {
                    moduleRegion.setStyle(DEFAULT_MODULE_STYLE);
                    module.deactivate();
                }
            });
            //Adding the constructed module region to its pane:
            modulePane.getChildren().addAll(moduleRegion, moduleName, moduleCode);
            ScrollPane scroll = (ScrollPane)ROOT_PANE.getChildren().get(2);
            ((FlowPane)scroll.getContent()).getChildren().add(modulePane);
        }
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
    private void sortModules(int sortBy, boolean reverse)
    {
        Collections.sort(MODULES_AVAILABLE, new Comparator<Module>()
        {
            @Override
            public int compare(Module o1, Module o2)
            {
                int toReturn = 0;
                switch (sortBy)
                {//Implement a different way of comparing objects depending on the required field.
                    case 0 :
                        if(o1.getModuleName().compareTo(o2.getModuleName()) > 0)
                            toReturn = 1;
                        else if(o1.getModuleName().compareTo(o2.getModuleName()) == 0)
                            toReturn = 0;
                        else
                            toReturn = -1;break;
                    case 1 :
                        if(o1.getModuleCode().compareTo(o2.getModuleCode()) > 0)
                            toReturn = 1;
                        else if(o1.getModuleCode().compareTo(o2.getModuleCode()) == 0)
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
                        if(o1.getModuleCreators()[0].compareTo(o2.getModuleCreators()[0]) > 0)
                            toReturn = 1;
                        else if(o1.getModuleCreators()[0].compareTo(o2.getModuleCreators()[0]) == 0)
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
        ArrayList<Module> temp = new ArrayList<Module>(MODULES_AVAILABLE);
        for(Module module : temp)
        {
            if(!include.contains(module.getCategory()))
                MODULES_AVAILABLE.remove(module);
        }
        clearModules();
        renderModules();
        MODULES_AVAILABLE = temp;
    }

    /**
     * Initialises the Application by reading the module list file.
     */
    @Override
    public void init()
    {
        ManualListReader reader = new ManualListReader();
        try
        {
            MODULES_AVAILABLE = reader.readModuleList();
            sortModules(0, false);
        }
        catch(InputIOException e)
        {
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    exceptionOnBoot = true;
                }
            });
        }
    }

    /**
     * Main method - only purpose currently is to launch the FX Application.
     * @param args - Command line arguments.
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}