package display;

import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import manual.ManualCreator;
import manual.Module;

/**
 * Holds functionality relating to the creation and operation of a dialog to create a manual for the selected modules.
 */
public class MakeManualDialogCreator
{
    /**
     * Creates the dialog to generate a manual.
     */
    void generateManualDialog()
    {
        Dialog<Void> makeManualDialog = new Dialog<Void>();
        DialogPane mmDialogPane = new DialogPane();
        GridPane dialogGrid = new GridPane();
        dialogGrid.setHgap(5);
        dialogGrid.setVgap(5);
        mmDialogPane.setContent(dialogGrid);
        makeManualDialog.setDialogPane(mmDialogPane);
        makeManualDialog.setTitle("Export Manual");
        makeManualDialog.setHeaderText("Export Manual");
        makeManualDialog.setContentText("Choose your preferences below and hit OK.");

        CheckBox subcategories = new CheckBox("Create a category per letter");
        dialogGrid.add(subcategories, 0, 0);
        CheckBox splitNeedy = new CheckBox("Separate section for any needy modules at end");
        dialogGrid.add(splitNeedy, 0, 1);
        CheckBox splitVanilla = new CheckBox("Separate section for any vanilla modules after mods");
        dialogGrid.add(splitVanilla, 0, 2);

        makeManualDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        makeManualDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        makeManualDialog.setResultConverter(new Callback<ButtonType, Void>()
        {
            @Override
            public Void call(ButtonType buttonType)
            {
                if(buttonType == ButtonType.OK)
                {
                    ManualCreator manual = new ManualCreator("resources/manual.tex");
                    for(Module module : Main.MODULES_AVAILABLE)
                    {
                        if(module.isActive())
                            manual.addModule(module);
                    }
                    manual.setManualName("The Centurion - Manual");
                    try
                    {
                        manual.writeManual();
                    }
                    catch(Exception e){}
                }
                return null;
            }
        });
        makeManualDialog.showAndWait();
    }
}
