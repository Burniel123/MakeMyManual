package display;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;
import manual.ManualCreator;
import manual.Module;
import manual.OutputIOException;

import java.io.*;
import java.util.ArrayList;

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
        saveLocation.setTitle("Choose location to save manual");
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
                    ArrayList<Module> displayModules = new ArrayList<Module>(Main.MODULES_DISPLAYED);
                    Main.MODULES_DISPLAYED = new ArrayList<Module>(Main.MODULES_AVAILABLE);
                    SortDialogCreator sdc = new SortDialogCreator();
                    ArrayList<Module> needySection = new ArrayList<Module>();
                    if(splitNeedy.isSelected())
                    {
                        ArrayList<Module> temp = new ArrayList<Module>(Main.MODULES_DISPLAYED);
                        ArrayList<Integer> needyFilter = new ArrayList<Integer>();
                        needyFilter.add(2);
                        sdc.filterModules(needyFilter);
                        for(Module module : Main.MODULES_DISPLAYED)
                        {
                            if(module.isActive())
                                needySection.add(module);
                            temp.remove(module);
                        }
                        Main.MODULES_DISPLAYED = temp;
                    }
                    sdc.sortModules(0, false);
                    for(Module module : Main.MODULES_DISPLAYED)
                    {
                        if(module.isActive())
                            manual.addModule(module);
                    }
                    for(Module module : needySection)
                        manual.addModule(module);
                    manual.setManualName("The Centurion - Manual");
                    Main.MODULES_DISPLAYED = displayModules;
                    Main.clearModules();
                    Main.renderModules();
                    Runnable compile = new Runnable()
                    {//tex file creation and compilation will happen in a separate thread as it takes ages and is risky.
                        @Override
                        public void run()
                        {
                            try
                            {
                                manual.writeManual();
                                ProcessBuilder builder = new ProcessBuilder("pdflatex", manual.getTexFilePath());
                                builder.redirectErrorStream(true);
                                Process pro = builder.start();
                                BufferedReader inStrm = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                                System.out.println("Should be rolling");
                                String line = null;
                                while ((line = inStrm.readLine()) != null)
                                    System.out.print(line);
                            }
                            catch(OutputIOException e)
                            {//This exception will be thrown if there was an error writing to the tex file.
                                Platform.runLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Alert exceptionAlert = new Alert(Alert.AlertType.ERROR);
                                        exceptionAlert.setTitle("Error writing manual!");
                                        exceptionAlert.setHeaderText("Error encountered while writing pdf.\n" +
                                                "Do all working directories have appropriate permissions?");
                                        exceptionAlert.setContentText("Please try rebooting and/or reinstalling the application.\n" +
                                                "If problem persists, please contact Daniel Burton.");
                                        exceptionAlert.showAndWait();
                                        Platform.exit();
                                    }
                                });
                            }
                            catch(IOException e)
                            {//This exception will be thrown if there was an error compiling the pdf.
                                Platform.runLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Alert exceptionAlert = new Alert(Alert.AlertType.ERROR);
                                        exceptionAlert.setTitle("Error writing manual!");
                                        exceptionAlert.setHeaderText("Error encountered while compiling pdf.\n" +
                                                "Do all working directories have appropriate permissions?");
                                        exceptionAlert.setContentText("Please try rebooting and/or reinstalling the application.\n" +
                                                "If problem persists, please contact Daniel Burton.");
                                        exceptionAlert.showAndWait();
                                        Platform.exit();
                                    }
                                });
                            }
                        }
                    };
                    Thread compileThread = new Thread(compile);
                    compileThread.setDaemon(true);
                    compileThread.start();
                }
                return null;
            }
        });
        makeManualDialog.showAndWait();
    }
}
