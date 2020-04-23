package display;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import manual.Module;
import manual.ProfileException;
import manual.ProfileReader;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Wraps a VBox to design and manage the main frontend for the application.
 *
 * @author Daniel Burton
 */
public class RootPane extends VBox implements Sortable
{
    private final HBox topMenu = new HBox(2);
    private final HBox numSelectedBar = new HBox(2);
    private final VBox scrollContent = new VBox(5);
    private final ScrollPane scrollableWindow = new ScrollPane(scrollContent);
    private final FlowPane modulesPane = new FlowPane(2, 2);
    private final FlowPane needyPane = new FlowPane(2, 2);
    private final Label numSelectedLabel = new Label("Modules Selected");
    private final Button selectAllRegular = new Button("Select All");
    private final Button deselectAllRegular = new Button("Deselect All");
    private final Button selectAllNeedy = new Button("Select All");
    private final Button deselectAllNeedy = new Button("Deselect All");
    private final TextField searchBarInput = new TextField();
    private final Button searchBarSubmit = new Button("Search!");
    private final Button launchSortMenu = new Button("Sort");
    private final Button importProfile = new Button("Import Profile");
    private final Button presetsButton = new Button("Presets");
    private final Button makeIt = new Button("Make Manual");

    private static final Font TITLES_FONT = new Font("Arial Bold", 16);
    static final Background DEFAULT_MODULE_BACK = new Background(new BackgroundFill(Color.WHITE,
            new CornerRadii(3), new Insets(2)));
    static final Background SELECTED_MODULE_BACK = new Background(new BackgroundFill(Color.web("0x669900"),
            new CornerRadii(3), new Insets(2)));
    static final Border DEFAULT_BORDER = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
            new CornerRadii(3), BorderWidths.DEFAULT));
    static final String DEFAULT_BACK_STYLE = "-fx-background-color: #A9A9A9";

    /**
     * Creates an instance of the RootPane.
     * Note this does not display that instance (this is primarily done by the start(Stage) method), it merely
     * creates a Scene graph to be used.
     */
    RootPane()
    {
        //Labels setup:
        final Label regularLabel = new Label("Regular Modules");
        final Label needyLabel = new Label("Needy Modules");
        numSelectedLabel.setText("Modules Selected: 0/" + Main.MODULES_AVAILABLE.size());
        regularLabel.setFont(TITLES_FONT);
        needyLabel.setFont(TITLES_FONT);
        numSelectedLabel.setFont(TITLES_FONT);

        //Module panes setup:
        modulesPane.setPadding(new Insets(0,0,0,5));
        needyPane.setPadding(new Insets(0,0,0,5));

        //Scroll bar setup:
        scrollContent.setAlignment(Pos.CENTER);
        scrollContent.setStyle(DEFAULT_BACK_STYLE);
        scrollableWindow.setFitToHeight(true);
        scrollableWindow.setFitToWidth(true);
        scrollableWindow.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        //Select/Deselect Buttons & Panes setup:
        final HBox regularSelectionButtons = new HBox(2);
        final HBox needySelectionButtons = new HBox(2);
        regularSelectionButtons.getChildren().addAll(selectAllRegular, deselectAllRegular);
        needySelectionButtons.getChildren().addAll(selectAllNeedy, deselectAllNeedy);
        final BorderPane regularTitleBox = new BorderPane();
        final BorderPane needyTitleBox = new BorderPane();
        regularTitleBox.setCenter(regularLabel);
        regularTitleBox.setRight(regularSelectionButtons);
        needyTitleBox.setCenter(needyLabel);
        needyTitleBox.setRight(needySelectionButtons);
        scrollContent.getChildren().addAll(regularTitleBox, modulesPane, needyTitleBox, needyPane);

        //Top control menu setup:
        final Separator searchSeparator = new Separator();
        topMenu.setPadding(new Insets(5));
        searchBarInput.setMaxWidth(Double.MAX_VALUE);
        searchBarInput.setPromptText("Search for modules...");
        HBox.setHgrow(searchBarInput, Priority.ALWAYS);
        searchSeparator.setOrientation(Orientation.VERTICAL);
        topMenu.getChildren().addAll(searchBarInput, searchBarSubmit, searchSeparator, launchSortMenu,
                importProfile, presetsButton, makeIt);

        //"Num Selected" bar setup:
        numSelectedBar.setPadding(new Insets(0,5,0,5));
        numSelectedLabel.setFont(TITLES_FONT);

        setStyle(DEFAULT_BACK_STYLE);
        getChildren().addAll(topMenu, numSelectedBar, scrollableWindow);
        attachEventHandlers();
        renderModules();
    }

    /**
     * Adds event handlers to interactive elements of the display.
     */
    private void attachEventHandlers()
    {
        launchSortMenu.setOnMouseClicked(e ->
        {//Opens a sort menu dialog when the "Sort" button is pressed.
            SortDialog sd = new SortDialog();
            sd.setResultConversion();
            sd.initOwner(getScene().getWindow());
            sd.showAndWait();
        });

        importProfile.setOnMouseClicked(e ->
        {//Opens a file selection window and imports the profile selected.
            FileChooser fileToImport = new FileChooser();
            fileToImport.setTitle("Choose a profile to import");
            FileChooser.ExtensionFilter ef = new FileChooser.ExtensionFilter("JSON Profiles", ".json");
            fileToImport.getExtensionFilters().add(ef);
            ProfileReader jsonReader = new ProfileReader(fileToImport.showOpenDialog(getScene().getWindow()));

            Runnable highlightFromProfile = () ->
            {
                clearModules();
                renderModules();
                highlightAll(modulesPane.getChildren(), true);
                highlightAll(needyPane.getChildren(), true);
                numSelectedLabel.setText("Modules Selected: " + Main.numSelected + "/" + Main.MODULES_AVAILABLE.size());
            };

            Thread jsonReadThread = new Thread(() ->
            {
                try
                {
                    ArrayList<String> moduleCodes = jsonReader.readJson();
                    Collections.sort(moduleCodes);
                    ArrayList<Module> temp = new ArrayList<Module>(Main.MODULES_DISPLAYED);
                    Main.MODULES_DISPLAYED = new ArrayList<Module>();
                    for(int i = 0; i < moduleCodes.size(); i++)
                    {//For every module code to be used, locate it and add it to the list of modules to be displayed.
                        for(int j = 0; j < Main.MODULES_AVAILABLE.size(); j++)
                        {
                            if(Main.MODULES_AVAILABLE.get(j).getModuleCode().equals(moduleCodes.get(i)))
                            {
                                Main.MODULES_DISPLAYED.add(Main.MODULES_AVAILABLE.get(j));
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
                        exceptionAlert.initOwner(getScene().getWindow());
                        exceptionAlert.showAndWait();
                    });
                }
                catch(Exception ex)
                {//This exception occurs when the json file cannot be read at all.
                    Platform.runLater(() ->
                    {
                        ExceptionAlert exceptionAlert = new ExceptionAlert("Error loading profile!",
                                "Has it been edited or removed?");
                        exceptionAlert.initOwner(getScene().getWindow());
                        exceptionAlert.showAndWait();
                    });
                }
            });
            jsonReadThread.setDaemon(true);
            jsonReadThread.start();
        });
        //Applies a search to the modules when the search button is pressed:
        searchBarSubmit.setOnMouseClicked(e -> Main.searchModules(searchBarInput.getText()));

        searchBarInput.setOnKeyPressed(e ->
        {//Applies a search to the modules when the return key is pressed while the search bar is active.
            if (e.getCode() == KeyCode.ENTER)
                Main.searchModules(searchBarInput.getText());
        });
        selectAllRegular.setOnMouseClicked(e ->
        {//Selects all solvable modules when the "Select All" button is pressed.
            highlightAll(modulesPane.getChildren(), true);
            numSelectedLabel.setText("Modules Selected: " + Main.numSelected + "/" + Main.MODULES_AVAILABLE.size());
        });
        selectAllNeedy.setOnMouseClicked(e ->
        {//Selects all needy modules when the "Select All" button is pressed.
            highlightAll(modulesPane.getChildren(), true);
            numSelectedLabel.setText("Modules Selected: " + Main.numSelected + "/" + Main.MODULES_AVAILABLE.size());
        });
        deselectAllRegular.setOnMouseClicked(e ->
        {//Deselects any selected solvable modules when the "Deselect All" button is pressed.
            highlightAll(modulesPane.getChildren(), false);
            numSelectedLabel.setText("Modules Selected: " + Main.numSelected + "/" + Main.MODULES_AVAILABLE.size());
        });
        deselectAllNeedy.setOnMouseClicked(e ->
        {//Deselects any selected needy modules when the "Deselect All" button is pressed.
            highlightAll(modulesPane.getChildren(), false);
            numSelectedLabel.setText("Modules Selected: " + Main.numSelected + "/" + Main.MODULES_AVAILABLE.size());
        });
        makeIt.setOnMouseClicked(e ->
        {//Opens a dialog with options for pdf compilation when the "Make Manual" button is pressed.
            MakeManualDialog mmd = new MakeManualDialog();
            mmd.setupResultConverter();
            mmd.initOwner(getScene().getWindow());
            mmd.showAndWait();
            Main.numSelected = 0;
            numSelectedLabel.setText("Modules Selected: " + Main.numSelected + "/" + Main.MODULES_AVAILABLE.size());
        });
    }

    /**
     * Clear all modules off the screen, generally so they can be re-rendered.
     */
    void clearModules()
    {
        modulesPane.getChildren().clear();
        needyPane.getChildren().clear();
    }

    /**
     * Renders module regions to the stage.
     */
    void renderModules()
    {
        for(int i = 0; i < 2*Main.MODULES_DISPLAYED.size(); i++)
        {
            Module module = Main.MODULES_DISPLAYED.get(i%Main.MODULES_DISPLAYED.size());
            if((module.getCategory() != 2 && i < Main.MODULES_DISPLAYED.size()) ||
                    (module.getCategory() == 2 && i >= Main.MODULES_DISPLAYED.size()))
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
                        Main.numSelected++;
                    }
                    else
                    {
                        moduleRegion.setBackground(DEFAULT_MODULE_BACK);
                        module.deactivate();
                        Main.numSelected--;
                    }
                    numSelectedLabel.setText("Modules Selected: " +
                            Main.numSelected + "/" + Main.MODULES_AVAILABLE.size());
                });
                //Adding the constructed module region to its pane:
                modulePane.getChildren().addAll(moduleRegion, moduleName, moduleCode);
                if(module.getCategory() == 2)
                    needyPane.getChildren().add(modulePane);
                else
                    modulesPane.getChildren().add(modulePane);
            }
        }
    }

    /**
     * Highlights all modules in a given list and activates them as members of the set of modules to be
     * included in the manual.
     * @param modules - ObservableList of Nodes, containing modules to highlight.
     * @param select - true if the modules are to be selected, false if they are to be deselected.
     */
    private void highlightAll(ObservableList<Node> modules, boolean select)
    {
        sortModules(0, false);
        for (int i = 0; i < Main.MODULES_DISPLAYED.size(); i++)
        {
            for(int j = 0; j < modules.size(); j++)
            {
                StackPane modulePane = (StackPane) modules.get(j);
                if(((Label)modulePane.getChildren().get(2)).getText().equals(Main.MODULES_DISPLAYED.get(i).getModuleCode()))
                {
                    if(select && ((Region)modulePane.getChildren().get(0)).getBackground().equals(DEFAULT_MODULE_BACK))
                    {
                        Main.numSelected++;
                        ((Region)modulePane.getChildren().get(0)).setBackground(SELECTED_MODULE_BACK);
                        Main.MODULES_DISPLAYED.get(i).activate();
                    }
                    else if(!select && ((Region)modulePane.getChildren().get(0)).getBackground().equals(SELECTED_MODULE_BACK))
                    {
                        Main.numSelected--;
                        ((Region)modulePane.getChildren().get(0)).setBackground(DEFAULT_MODULE_BACK);
                        Main.MODULES_DISPLAYED.get(i).deactivate();
                    }
                    break;
                }
            }
        }
    }
}