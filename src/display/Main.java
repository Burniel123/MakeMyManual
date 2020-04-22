package display;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import manual.Module;
import manual.*;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Main application class for MakeMyManual.
 *
 * @author Daniel Burton
 */
public class Main extends Application implements Sortable
{
    static RootPane ROOT_PANE = null;
    static final String DEFAULT_BACK_STYLE = "-fx-background-color: #A9A9A9";
    static final Background DEFAULT_MODULE_BACK = new Background(new BackgroundFill(Color.WHITE,
            new CornerRadii(3), new Insets(2)));
    static final Background SELECTED_MODULE_BACK = new Background(new BackgroundFill(Color.web("0x669900"),
            new CornerRadii(3), new Insets(2)));
    static final Border DEFAULT_BORDER = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
            new CornerRadii(3), BorderWidths.DEFAULT));
    static final Font TITLES_FONT = new Font("Arial Bold", 16);
    static final Image DEFAULT_ICON = new Image("file:resources/icon.png");

    private static boolean exceptionOnBoot = false;
    private static InputIOException bootException = null;
    static int numSelected = 0;

    static ArrayList<Module> MODULES_AVAILABLE = new ArrayList<Module>();//Please don't change after init.
    static ArrayList<Module> MODULES_DISPLAYED = new ArrayList<Module>();

    /**
     * Starts the application, setting up and displaying the stage.
     * @param primaryStage - the main stage for the application.
     */
    @Override
    public void start(Stage primaryStage)
    {
        ROOT_PANE = new RootPane();
        /*//Setting up an area for the modules:
        final FlowPane modulesPane = new FlowPane(2, 2);
        modulesPane.setPadding(new Insets(0, 0, 0, 5));
        final FlowPane needyPane = new FlowPane(2, 2);
        needyPane.setPadding(new Insets(0, 0, 0, 5));
        final VBox scrollContent = new VBox(5);
        ROOT_PANE.setStyle(DEFAULT_BACK_STYLE);
        scrollContent.setStyle(DEFAULT_BACK_STYLE);

        final Label regularLabel = new Label("Regular Modules");
        regularLabel.setFont(TITLES_FONT);
        final BorderPane regularTitleBox = new BorderPane();
        final Label needyLabel = new Label("Needy Modules");
        needyLabel.setFont(TITLES_FONT);
        final BorderPane needyTitleBox = new BorderPane();

        scrollContent.getChildren().addAll(regularTitleBox, modulesPane, needyTitleBox, needyPane);
        scrollContent.setAlignment(Pos.CENTER);
        final ScrollPane scrollableWindow = new ScrollPane(scrollContent);
        scrollableWindow.setFitToHeight(true);
        scrollableWindow.setFitToWidth(true);
        scrollableWindow.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        final Button selectAllRegular = new Button("Select All");
        final Button deselectAllRegular = new Button("Deselect All");
        final Button selectAllNeedy = new Button("Select All");
        final Button deselectAllNeedy = new Button("Deselect All");
        final HBox regularSelectionButtons = new HBox(2);
        regularSelectionButtons.getChildren().addAll(selectAllRegular, deselectAllRegular);
        final HBox needySelectionButtons = new HBox(2);
        needySelectionButtons.getChildren().addAll(selectAllNeedy, deselectAllNeedy);

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
        final Button importProfile = new Button("Import Profile");
        final Button presetsButton = new Button("Presets");
        final Button makeIt = new Button("Make My Manual!");
        topMenu.getChildren().addAll(searchBarInput, searchBarSubmit, searchSeparator,
                sortMenu, importProfile, presetsButton, makeIt);
        //Setting up components for the "num selected" bar.
        final HBox numSelectedBar = new HBox(2);
        final Label numSelected = new Label("Modules Selected: 0/" + MODULES_AVAILABLE.size());
        numSelectedBar.setPadding(new Insets(0, 5, 0, 5));
        numSelected.setFont(TITLES_FONT);
        searchBarInput.setPromptText("Search for modules...");
        regularTitleBox.setCenter(regularLabel);
        regularTitleBox.setRight(regularSelectionButtons);
        needyTitleBox.setCenter(needyLabel);
        needyTitleBox.setRight(needySelectionButtons);
        numSelectedBar.getChildren().add(numSelected);
        ROOT_PANE.getChildren().addAll(topMenu, numSelectedBar);
        ROOT_PANE.getChildren().add(scrollableWindow);
        renderModules();

        //EVENT HANDLERS:
        sortMenu.setOnMouseClicked(e ->
        {
            SortDialog sd = new SortDialog();
            sd.setResultConversion();
            sd.initOwner(primaryStage);
            sd.showAndWait();
        });
        importProfile.setOnMouseClicked(e ->
        {
            FileChooser fileToImport = new FileChooser();
            fileToImport.setTitle("Choose a profile to import");
            FileChooser.ExtensionFilter ef = new FileChooser.ExtensionFilter("JSON Profiles", ".json");
            fileToImport.getExtensionFilters().add(ef);
            ProfileReader jsonReader = new ProfileReader(fileToImport.showOpenDialog(primaryStage));

            Runnable highlightFromProfile = () ->
            {
                clearModules();
                renderModules();
                ScrollPane scroll = (ScrollPane)ROOT_PANE.getChildren().get(2);
                ObservableList<Node> modulePanes = ((FlowPane)((VBox)scroll.getContent()).getChildren().get(1)).getChildren();
                ObservableList<Node> needyPanes = ((FlowPane)((VBox)scroll.getContent()).getChildren().get(3)).getChildren();
                highlightAll(modulePanes, true);
                highlightAll(needyPanes, true);
                numSelected.setText("Modules Selected: " + Main.numSelected + "/" + MODULES_AVAILABLE.size());
            };

            Thread jsonReadThread = new Thread(() ->
            {
                try
                {
                    ArrayList<String> moduleCodes = jsonReader.readJson();
                    Collections.sort(moduleCodes);
                    ArrayList<Module> temp = new ArrayList<Module>(MODULES_DISPLAYED);
                    MODULES_DISPLAYED = new ArrayList<Module>();
                    for(int i = 0; i < moduleCodes.size(); i++)
                    {//For every module code to be used, locate it and add it to the list of modules to be displayed.
                        for(int j = 0; j < MODULES_AVAILABLE.size(); j++)
                        {
                            if(MODULES_AVAILABLE.get(j).getModuleCode().equals(moduleCodes.get(i)))
                            {
                                MODULES_DISPLAYED.add(MODULES_AVAILABLE.get(j));
                                break;
                            }
                        }
                    }
                    Platform.runLater(highlightFromProfile);
                }
                catch(ProfileException pe)
                {//This exception occurs when there is no "enabled" list in the profile.
                    Platform.runLater(() ->
                    {
                        ExceptionAlert exceptionAlert = new ExceptionAlert(pe);
                        exceptionAlert.initOwner(primaryStage);
                        exceptionAlert.showAndWait();
                    });
                }
                catch(Exception ex)
                {//This exception occurs when the json file cannot be read at all.
                    Platform.runLater(() ->
                    {
                        ExceptionAlert exceptionAlert = new ExceptionAlert("Error loading profile!",
                                "Has it been edited or removed?");
                        exceptionAlert.initOwner(primaryStage);
                        exceptionAlert.showAndWait();
                    });
                }
            });
            jsonReadThread.setDaemon(true);
            jsonReadThread.start();
        });
        searchBarSubmit.setOnMouseClicked(e ->
        {
            searchModules(searchBarInput.getText());
        });
        searchBarInput.setOnKeyPressed(e ->
        {
            if (e.getCode() == KeyCode.ENTER)
                searchModules(searchBarInput.getText());
        });
        selectAllRegular.setOnMouseClicked(e ->
        {
            ObservableList<Node> modulePanes = modulesPane.getChildren();
            highlightAll(modulePanes, true);
            numSelected.setText("Modules Selected: " + Main.numSelected + "/" + MODULES_AVAILABLE.size());
        });
        selectAllNeedy.setOnMouseClicked(e ->
        {
            ObservableList<Node> modulePanes = needyPane.getChildren();
            highlightAll(modulePanes, true);
            numSelected.setText("Modules Selected: " + Main.numSelected + "/" + MODULES_AVAILABLE.size());
        });
        deselectAllRegular.setOnMouseClicked(e ->
        {
            ObservableList<Node> modulePanes = modulesPane.getChildren();
            highlightAll(modulePanes, false);
            numSelected.setText("Modules Selected: " + Main.numSelected + "/" + MODULES_AVAILABLE.size());
        });
        deselectAllNeedy.setOnMouseClicked(e ->
        {
            ObservableList<Node> modulePanes = needyPane.getChildren();
            highlightAll(modulePanes, false);
            numSelected.setText("Modules Selected: " + Main.numSelected + "/" + MODULES_AVAILABLE.size());
        });
        makeIt.setOnMouseClicked(e ->
        {
            MakeManualDialog mmd = new MakeManualDialog();
            mmd.setupResultConverter();
            mmd.initOwner(primaryStage);
            mmd.showAndWait();
            Main.numSelected = 0;
            numSelected.setText("Modules Selected: " + Main.numSelected + "/" + MODULES_AVAILABLE.size());
        });*/

        final Scene scene = new Scene(ROOT_PANE);
        primaryStage.setTitle("MakeMyManual");
        primaryStage.setScene(scene);
        primaryStage.setWidth(750);
        primaryStage.setHeight(500);
        primaryStage.getIcons().add(DEFAULT_ICON);

        if (exceptionOnBoot)
        {
            ExceptionAlert exceptionAlert = new ExceptionAlert(bootException);
            exceptionAlert.initOwner(primaryStage);
            exceptionAlert.showAndWait();
            Platform.exit();
        }
        else
            primaryStage.show();
    }

 /*   *//**
     * Clear all modules off the screen, generally so they can be re-rendered.
     *//*
    static void clearModules()
    {
        ScrollPane scroll = (ScrollPane)ROOT_PANE.getChildren().get(2);
        ((FlowPane)((VBox)scroll.getContent()).getChildren().get(1)).getChildren().clear();
        ((FlowPane)((VBox)scroll.getContent()).getChildren().get(3)).getChildren().clear();
    }

    *//**
     * Renders module regions to the stage.
     *//*
    static void renderModules()
    {
        for(int i = 0; i < 2*MODULES_DISPLAYED.size(); i++)
        {
            Module module = MODULES_DISPLAYED.get(i%MODULES_DISPLAYED.size());
            if((module.getCategory() != 2 && i < MODULES_DISPLAYED.size()) ||
                    (module.getCategory() == 2 && i >= MODULES_DISPLAYED.size()))
            {//If this isn't a needy module, display it in the top half. If it is, put it in the bottom half.
                //Components to build each module region:
                final StackPane modulePane = new StackPane();
                final Region moduleRegion = new Region();
                final Label moduleName = new Label(module.getModuleName() + "\n\n");
                moduleName.setPadding(new Insets(2));
                modulePane.setAlignment(Pos.BOTTOM_CENTER);
                final Label moduleCode = new Label(module.getModuleCode());
                moduleCode.setFont(new Font(7));
                moduleRegion.setMinSize(100, 20);
                moduleRegion.setBackground(DEFAULT_MODULE_BACK);
                moduleRegion.setBorder(DEFAULT_BORDER);
                //Event handling code for each module:
                modulePane.setOnMouseClicked(e ->
                {
                    if(moduleRegion.getBackground().equals(DEFAULT_MODULE_BACK))
                    {
                        moduleRegion.setBackground(SELECTED_MODULE_BACK);
                        module.activate();
                        numSelected++;
                    }
                    else
                    {
                        moduleRegion.setBackground(DEFAULT_MODULE_BACK);
                        module.deactivate();
                        numSelected--;
                    }
                    ((Label)((HBox)ROOT_PANE.getChildren().get(1)).getChildren().get(0)).setText("Modules Selected: " +
                            numSelected + "/" + MODULES_AVAILABLE.size());
                });
                //Adding the constructed module region to its pane:
                modulePane.getChildren().addAll(moduleRegion, moduleName, moduleCode);
                ScrollPane scroll = (ScrollPane)ROOT_PANE.getChildren().get(2);
                if(module.getCategory() == 2)
                    ((FlowPane)((VBox)scroll.getContent()).getChildren().get(3)).getChildren().add(modulePane);
                else
                    ((FlowPane)((VBox)scroll.getContent()).getChildren().get(1)).getChildren().add(modulePane);
            }
        }
    }*/

    /**
     * Filters modules displayed by a provided search term.
     * @param searchTerm - term to search by - only modules whose names contain this term will be featured.
     */
    static void searchModules(String searchTerm)
    {
            ArrayList<Module> temp = new ArrayList<Module>(MODULES_DISPLAYED);
            for(Module module : temp)
            {
                if(!module.getModuleName().toLowerCase().contains(searchTerm.toLowerCase()))
                    MODULES_DISPLAYED.remove(module);
            }
        ROOT_PANE.clearModules();
        ROOT_PANE.renderModules();
        MODULES_DISPLAYED = temp;
    }

   /* *//**
     * Highlights all modules in a given list and activates them as members of the set of modules to be
     * included in the manual.
     * @param modules - ObservableList of Nodes, containing modules to highlight.
     * @param select - true if the modules are to be selected, false if they are to be deselected.
     *//*
    private void highlightAll(ObservableList<Node> modules, boolean select)
    {
        sortModules(0, false);
        for (int i = 0; i < MODULES_DISPLAYED.size(); i++)
        {
            for(int j = 0; j < modules.size(); j++)
            {
                StackPane modulePane = (StackPane) modules.get(j);
                if(((Label)modulePane.getChildren().get(2)).getText().equals(MODULES_DISPLAYED.get(i).getModuleCode()))
                {
                    if(select && ((Region)modulePane.getChildren().get(0)).getBackground().equals(DEFAULT_MODULE_BACK))
                    {
                        Main.numSelected++;
                        ((Region)modulePane.getChildren().get(0)).setBackground(SELECTED_MODULE_BACK);
                        MODULES_DISPLAYED.get(i).activate();
                    }
                    else if(!select && ((Region)modulePane.getChildren().get(0)).getBackground().equals(SELECTED_MODULE_BACK))
                    {
                        Main.numSelected--;
                        ((Region)modulePane.getChildren().get(0)).setBackground(DEFAULT_MODULE_BACK);
                        MODULES_DISPLAYED.get(i).deactivate();
                    }
                    break;
                }
            }
        }
    }*/

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
            MODULES_DISPLAYED = new ArrayList<Module>(MODULES_AVAILABLE);
            /*SortDialogCreator sdc = new SortDialogCreator();*/
            sortModules(0, false);
        }
        catch(InputIOException e)
        {
            Platform.runLater(new Runnable()
            {//If an exception was thrown, flag that an exception should be thrown.
                @Override
                public void run()
                {
                    exceptionOnBoot = true;
                    bootException = e;
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