package display;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import manual.Module;

/**
 * Wraps a StackPane to design and manage the display for a module.
 *
 * @author Daniel Burton
 */
public class ModulePane extends StackPane
{
    private final Region moduleRegion = new Region();
    private final Label moduleNameLabel = new Label();
    private final Label moduleCodeLabel = new Label();

    static final Background DEFAULT_MODULE_BACK = new Background(new BackgroundFill(Color.WHITE,
            new CornerRadii(3), new Insets(2)));
    static final Background SELECTED_MODULE_BACK = new Background(new BackgroundFill(Color.web("0x669900"),
            new CornerRadii(3), new Insets(2)));
    static final Border DEFAULT_BORDER = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
            new CornerRadii(3), BorderWidths.DEFAULT));

    /**
     * Creates a new ModulePane to be put on the display.
     * @param module - the Module to display.
     */
    ModulePane(Module module)
    {
        //Region setup:
        moduleRegion.setMinSize(100, 20);
        moduleRegion.setBackground(DEFAULT_MODULE_BACK);
        moduleRegion.setBorder(DEFAULT_BORDER);

        //Labels setup:
        moduleNameLabel.setPadding(new Insets(2));
        moduleNameLabel.setText(module.getModuleName() + "\n\n");
        moduleCodeLabel.setFont(new Font(7));
        moduleCodeLabel.setText(module.getModuleCode());

        //Event handler for clicking on this module pane:
        setOnMouseClicked(e ->
        {
            if(!isSelected())
            {
                module.activate();
                Main.numSelectedProperty.set(Main.numSelectedProperty.get()+1);
            }
            else
            {
                module.deactivate();
                Main.numSelectedProperty.set(Main.numSelectedProperty.get()-1);
            }
            invertCol();
        });

        //Configure module pane as a whole:
        setAlignment(Pos.BOTTOM_CENTER);
        getChildren().addAll(moduleRegion, moduleNameLabel, moduleCodeLabel);
    }

    /**
     * Changes the display of the module from selected to deselected or vice-versa.
     */
    void invertCol()
    {
        if(!isSelected())//If module is not already selected, select it.
            moduleRegion.setBackground(SELECTED_MODULE_BACK);
        else //If module is already selected, deselect it.
            moduleRegion.setBackground(DEFAULT_MODULE_BACK);
    }

    /**
     * Determines whether or not this module is selected.
     * @return true if the colour of the module indicates it is selected, false otherwise.
     */
    boolean isSelected()
    {
        return moduleRegion.getBackground().equals(SELECTED_MODULE_BACK);
    }

    /**
     * Obtains the module code for the module represented by this pane.
     * @return a String corresponding to the in-game module code for this pane's module.
     */
    String getModuleCodeContent()
    {
        return moduleCodeLabel.getText();
    }
}
