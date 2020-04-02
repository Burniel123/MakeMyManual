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
import manual.InputIOException;
import manual.ManualListReader;
import manual.Module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Main extends Application
{
    private static final VBox ROOT_PANE = new VBox(3);
    private static final String DEFAULT_MODULE_STYLE = "-fx-background-color: #f25d55";
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
        final TextField searchBarInput = new TextField("Search for mods...");
        searchBarInput.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchBarInput, Priority.ALWAYS);
        final Button searchBarSubmit = new Button("Search!");
        final Separator searchSeparator = new Separator();
        searchSeparator.setOrientation(Orientation.VERTICAL);
        final Button filterMenu = new Button("Filter");
        final Button presetsButton = new Button("Presets");
        topMenu.getChildren().addAll(searchBarInput, searchBarSubmit, searchSeparator,
                filterMenu, presetsButton);
        //Setting up components for the "num selected" bar.
        final HBox numSelectedBar = new HBox(2);
        final Label numSelected = new Label("Modules Selected: 0/98");
        numSelectedBar.getChildren().add(numSelected);
        ROOT_PANE.getChildren().addAll(topMenu, numSelectedBar);
        ROOT_PANE.getChildren().add(scrollableWindow);
        renderModules();


        final Scene scene = new Scene(ROOT_PANE);
        primaryStage.setTitle("MakeMyManual");
        primaryStage.setScene(scene);
        primaryStage.setWidth(750);
        primaryStage.setHeight(500);
/*        primaryStage.setMaxWidth(1000);
        primaryStage.setMaxHeight(1000);*/

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
            moduleRegion.setStyle(DEFAULT_MODULE_STYLE);
            moduleRegion.setMinSize(100, 20);
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