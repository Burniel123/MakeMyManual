package org.makemymanual.display;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.makemymanual.manual.Module;
import org.makemymanual.manual.ProfileException;
import org.makemymanual.manual.ProfileReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Flow;

/**
 * Wraps a VBox to design and manage the main frontend for the application.
 *
 * @author Daniel Burton
 */
public class RootPane extends VBox implements Sortable
{
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
    private final Button makeIt = new Button("Make Manual");

    private static final Font TITLES_FONT = new Font("Arial Bold", 16);
    static final String DEFAULT_BACK_STYLE = "-fx-background-color: #A9A9A9";
    private ModulePane lastSelected = null;
    private boolean shiftKeyDown = false;

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
        VBox scrollContent = new VBox(5);
        scrollContent.setAlignment(Pos.CENTER);
        scrollContent.setStyle(DEFAULT_BACK_STYLE);
        ScrollPane scrollableWindow = new ScrollPane(scrollContent);
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
        HBox topMenu = new HBox(2);
        topMenu.setPadding(new Insets(5));
        searchBarInput.setMaxWidth(Double.MAX_VALUE);
        searchBarInput.setPromptText("Search for modules...");
        HBox.setHgrow(searchBarInput, Priority.ALWAYS);
        searchSeparator.setOrientation(Orientation.VERTICAL);
        Button presetsButton = new Button("Presets");
        topMenu.getChildren().addAll(searchBarInput, searchBarSubmit, searchSeparator, launchSortMenu,
                importProfile, presetsButton, makeIt);

        //"Num Selected" bar setup:
        HBox numSelectedBar = new HBox(2);
        numSelectedBar.setPadding(new Insets(0,5,0,5));
        numSelectedBar.getChildren().addAll(numSelectedLabel);
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
            FileChooser.ExtensionFilter ef = new FileChooser.ExtensionFilter("JSON Profiles", "*.json");
            fileToImport.getExtensionFilters().add(ef);
            ProfileReader jsonReader = new ProfileReader(fileToImport.showOpenDialog(getScene().getWindow()));

            Thread jsonReadThread = new Thread(() ->
            {//Separate reading a profile into a separate thread to prevent frontend freezing.
                try
                {
                    ArrayList<String> moduleCodes = jsonReader.readJson();
                    Collections.sort(moduleCodes);

                    for(int i = 0; i < moduleCodes.size(); i++)
                    {//For every module code to be used, locate it and add it to the list of modules to be displayed.
                        for(Node n : modulesPane.getChildren())
                        {
                            if(moduleCodes.get(i).equals(((ModulePane)n).getModuleCodeContent()))
                            {
                                Platform.runLater(() ->
                                {//Now return to the JavaFX Application thread to update modules' displays.
                                    if(!((ModulePane) n).isSelected())
                                    {
                                        ((ModulePane) n).invertCol();
                                        Main.numSelectedProperty.set(Main.numSelectedProperty.get()+1);
                                    }
                                });
                                break;
                            }
                        }

                        for(Node n : needyPane.getChildren())
                        {//Exactly as above for the pane of needy modules.
                            if(moduleCodes.get(i).equals(((ModulePane)n).getModuleCodeContent()))
                            {
                                Platform.runLater(() ->
                                {//Now return to the JavaFX Application thread to update modules' displays.
                                    if(!((ModulePane) n).isSelected())
                                    {
                                        ((ModulePane) n).invertCol();
                                        Main.numSelectedProperty.set(Main.numSelectedProperty.get()+1);
                                    }
                                });
                                break;
                            }
                        }
                        for(Module m : Main.MODULES_AVAILABLE)
                        {//"Activate" all Module objects which were included in the profile.
                            if(m.getModuleCode().equals(moduleCodes.get(i)))
                            {
                                m.activate();
                                break;
                            }
                        }
                    }
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
            //numSelectedLabel.setText("Modules Selected: " + Main.numSelected + "/" + Main.MODULES_AVAILABLE.size());
        });
        selectAllNeedy.setOnMouseClicked(e ->
        {//Selects all needy modules when the "Select All" button is pressed.
            highlightAll(needyPane.getChildren(), true);
            //numSelectedLabel.setText("Modules Selected: " + Main.numSelected + "/" + Main.MODULES_AVAILABLE.size());
        });
        deselectAllRegular.setOnMouseClicked(e ->
        {//Deselects any selected solvable modules when the "Deselect All" button is pressed.
            highlightAll(modulesPane.getChildren(), false);
            //numSelectedLabel.setText("Modules Selected: " + Main.numSelected + "/" + Main.MODULES_AVAILABLE.size());
        });
        deselectAllNeedy.setOnMouseClicked(e ->
        {//Deselects any selected needy modules when the "Deselect All" button is pressed.
            highlightAll(needyPane.getChildren(), false);
            //numSelectedLabel.setText("Modules Selected: " + Main.numSelected + "/" + Main.MODULES_AVAILABLE.size());
        });
        makeIt.setOnMouseClicked(e ->
        {//Opens a dialog with options for pdf compilation when the "Make Manual" button is pressed.
            MakeManualDialog mmd = new MakeManualDialog();
            mmd.setupResultConverter();
            mmd.initOwner(getScene().getWindow());
            mmd.showAndWait();
            Main.numSelected = 0;
            //numSelectedLabel.setText("Modules Selected: " + Main.numSelected + "/" + Main.MODULES_AVAILABLE.size());
        });
        setOnKeyPressed(e ->
        {
            if(e.getCode().equals(KeyCode.SHIFT))
                shiftKeyDown = true;
        });
        setOnKeyReleased(e ->
        {
            if(e.getCode().equals(KeyCode.SHIFT))
                shiftKeyDown = false;
        });

        Main.numSelectedProperty.addListener(new ChangeListener<Number>()
        {//Update the "Num Selected" label whenever the numSelectedProperty changes.
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1)
            {
                numSelectedLabel.setText("Modules Selected: " + t1 + "/" + Main.MODULES_AVAILABLE.size());
            }
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
                ModulePane modulePane = new ModulePane(module);

                //Event handler for clicking on this module pane:
                modulePane.setOnMouseClicked(e ->
                {
                    if(shiftKeyDown)
                    {//If the shift key is being held, all modules from the previously selected module to the selected module must be selected.
                        boolean startingModulePassed = false;
                        FlowPane fp = new FlowPane();

                        if(module.getCategory() != 2)
                            fp = modulesPane;//Only apply the shift-click feature if we're selecting in the correct category of module.
                        else
                            fp = needyPane;

                        for(Object obj : fp.getChildren())
                        {//Work through all module panes and highlight those between the previous select and this select.
                            ModulePane mp = (ModulePane)obj;
                            if(mp.getModuleCodeContent().equals(lastSelected.getModuleCodeContent()))
                                startingModulePassed = true; //Flags that we've hit the previously-selected module.
                            else if(mp.getModuleCodeContent().equals(modulePane.getModuleCodeContent()) && !modulePane.isSelected())
                            {//If we've hit the module that was clicked, select it and stop going through modules.
                                module.activate();
                                Main.numSelectedProperty.set(Main.numSelectedProperty.get() + 1);
                                mp.invertCol();
                                lastSelected = modulePane;
                                break;
                            }
                            else if(mp.getModuleCodeContent().equals(modulePane.getModuleCodeContent()))
                                break; //If we've hit the module that was clicked and it has already been selected, stop going through modules.
                            else if(startingModulePassed && !mp.isSelected())
                            {//Select all deselected modules between the previous and current modules.
                                //module.activate();
                                for(Module mod : Main.MODULES_DISPLAYED)
                                {
                                    if(mod.getModuleCode().equals(mp.getModuleCodeContent()))
                                    {
                                        mod.activate();
                                        break;
                                    }
                                }
                                Main.numSelectedProperty.set(Main.numSelectedProperty.get() + 1);
                                mp.invertCol();
                            }
                        }
                    }
                    else
                    {//If shift was not held, select normally.
                        if(!modulePane.isSelected())
                        {
                            module.activate();
                            Main.numSelectedProperty.set(Main.numSelectedProperty.get()+1);
                            lastSelected = modulePane;
                        }
                        else
                        {
                            module.deactivate();
                            Main.numSelectedProperty.set(Main.numSelectedProperty.get()-1);
                        }
                        modulePane.invertCol();
                    }
                });

                if(module.isActive())
                    modulePane.invertCol();
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
        {//For each module on the screen, determine whether we want it highlighted and if it is already highlighted.
            for(int j = 0; j < modules.size(); j++)
            {
                ModulePane modulePane = (ModulePane)modules.get(j);
                if(modulePane.getModuleCodeContent().equals(Main.MODULES_DISPLAYED.get(i).getModuleCode()))
                {
                    if(select && !modulePane.isSelected())
                    {
                        modulePane.invertCol();
                        Main.numSelectedProperty.set(Main.numSelectedProperty.get()+1);
                        Main.MODULES_DISPLAYED.get(i).activate();
                    }
                    else if(!select && modulePane.isSelected())
                    {
                        modulePane.invertCol();
                        Main.numSelectedProperty.set(Main.numSelectedProperty.get()-1);
                        Main.MODULES_DISPLAYED.get(i).deactivate();
                    }
                    break;
                }
            }
        }
    }

    /**
     * Obtains the contents of the search bar, for use in filtering.
     * @return the String contained within the search bar.
     */
    String getSearchBarContents()
    {
        return searchBarInput.getText();
    }
}