package display;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;
import manual.ManualCreator;
import manual.Module;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.cert.Extension;

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

        Button chooseDestination = new Button("Choose manual location...");
        dialogGrid.add(chooseDestination, 0, 3);
        FileChooser saveLocation = new FileChooser();
        ExtensionFilter filter = new ExtensionFilter("LaTeX file: .tex", ".tex");
        saveLocation.getExtensionFilters().add(filter);
        ManualCreator manual = new ManualCreator("resources/manual.tex");
        chooseDestination.setOnMouseClicked(e ->
        {
                File save = saveLocation.showSaveDialog(new Stage());
                manual.setOutputTexFile(save);
        });

        makeManualDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        makeManualDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        makeManualDialog.setResultConverter(new Callback<ButtonType, Void>()
        {
            @Override
            public Void call(ButtonType buttonType)
            {
                if(buttonType == ButtonType.OK)
                {
                    for(Module module : Main.MODULES_AVAILABLE)
                    {
                        if(module.isActive())
                            manual.addModule(module);
                    }
                    manual.setManualName("The Centurion - Manual");
                    try
                    {
                        manual.writeManual();
                        ProcessBuilder builder = new ProcessBuilder("pdflatex", manual.getTexFilePath());
                        builder.redirectErrorStream(true);
                        Process pro = builder.start();
                        BufferedReader inStrm = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                        OutputStream outStrm = pro.getOutputStream();
                        System.out.println("Should be rolling");
                        String line = null;
                        while((line = inStrm.readLine()) != null)
                            System.out.print(line);
                    }
                    catch(Exception e){System.out.println(e);}
                }
                return null;
            }
        });
        makeManualDialog.showAndWait();
    }
}
