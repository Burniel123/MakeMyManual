package display;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import manual.InputIOException;
import manual.ManualListReader;
import manual.Module;

import java.util.ArrayList;

public class Main extends Application
{
    static final VBox ROOT_PANE = new VBox(3);
    static final String DEFAULT_MODULE_STYLE = "-fx-background-color: #f25d55";
    static final String SELECTED_MODULE_STYLE = "-fx-background-color: #669900";
    static final String DEFAULT_BACK_STYLE = "-fx-background-color: #e5cb90";
    static boolean exceptionOnBoot = false;
    static int numSelected = 0;

    static ArrayList<Module> MODULES_AVAILABLE = new ArrayList<Module>();

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
        final TextField searchBarInput = new TextField();
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
        final Label numSelected = new Label("Modules Selected: 0/" + MODULES_AVAILABLE.size());
        searchBarInput.setPromptText("Search for modules...");
        numSelectedBar.getChildren().add(numSelected);
        ROOT_PANE.getChildren().addAll(topMenu, numSelectedBar);
        ROOT_PANE.getChildren().add(scrollableWindow);
        renderModules();

        SortDialogCreator sdc = new SortDialogCreator();
        sortMenu.setOnMouseClicked(e -> sdc.applyModuleSort());
        searchBarSubmit.setOnMouseClicked(e -> searchModules(searchBarInput.getText()));
        searchBarInput.setOnKeyPressed(e ->
        {
            if(e.getCode() == KeyCode.ENTER)
                searchModules(searchBarInput.getText());
        });
        makeIt.setOnMouseClicked(e ->
        {
            MakeManualDialogCreator mmdc = new MakeManualDialogCreator();
            mmdc.generateManualDialog();
        });

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
     * Clear all modules off the screen, generally so they can be re-rendered.
     */
    static void clearModules()
    {
        ScrollPane scroll = (ScrollPane)ROOT_PANE.getChildren().get(2);
        ((FlowPane)scroll.getContent()).getChildren().clear();
    }

    /**
     * Renders module regions to the stage.
     */
    static void renderModules()
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
                    numSelected++;
                }
                else
                {
                    moduleRegion.setStyle(DEFAULT_MODULE_STYLE);
                    module.deactivate();
                    numSelected--;
                }
                ((Label)((HBox)ROOT_PANE.getChildren().get(1)).getChildren().get(0)).setText("Modules Selected: " +
                        numSelected + "/" + MODULES_AVAILABLE.size());
            });
            //Adding the constructed module region to its pane:
            modulePane.getChildren().addAll(moduleRegion, moduleName, moduleCode);
            ScrollPane scroll = (ScrollPane)ROOT_PANE.getChildren().get(2);
            ((FlowPane)scroll.getContent()).getChildren().add(modulePane);
        }
    }

    static void searchModules(String searchTerm)
    {
        ArrayList<Module> temp = new ArrayList<Module>(MODULES_AVAILABLE);
        for(Module module : temp)
        {
            if(!module.getModuleName().toLowerCase().contains(searchTerm.toLowerCase()))
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
            SortDialogCreator sdc = new SortDialogCreator();
            sdc.sortModules(0, false);
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