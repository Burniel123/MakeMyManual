package display;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;
import manual.ManualCreator;
import manual.Module;
import manual.OutputIOException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Holds functionality relating to the creation and operation of a dialog to create a manual for the selected modules.
 */
public class MakeManualDialog extends Dialog<Void> implements Sortable
{
    private ManualCreator manual = null;
    private ProgressDialog pd = null;
    private ProgressManager pm = null;

    private CheckBox splitVanilla = null;
    private CheckBox splitNeedy = null;
    private TextField nameManual = null;


    /**
     * Creates the dialog to generate a manual.
     */
    MakeManualDialog()
    {
        DialogPane mmDialogPane = new DialogPane();
        GridPane dialogGrid = new GridPane();
        dialogGrid.setHgap(5);
        dialogGrid.setVgap(5);
        mmDialogPane.setContent(dialogGrid);
        setDialogPane(mmDialogPane);
        setTitle("Export Manual");
        setHeaderText("Export Manual");
        setContentText("Choose your preferences below and hit OK.");

        CheckBox subcategories = new CheckBox("Create a category per letter");
        dialogGrid.add(subcategories, 0, 0);
        splitNeedy = new CheckBox("Separate section for any needy modules at end");
        dialogGrid.add(splitNeedy, 0, 1);
        splitVanilla = new CheckBox("Separate section for any vanilla modules after mods");
        dialogGrid.add(splitVanilla, 0, 2);

        final Button chooseDestination = new Button("Choose manual location...");
        dialogGrid.add(chooseDestination, 0, 3);
        FileChooser saveLocation = new FileChooser();
        ExtensionFilter filter = new ExtensionFilter("Protable Document Format: .pdf", ".pdf");
        saveLocation.getExtensionFilters().add(filter);
        saveLocation.setTitle("Choose location to save manual");
        manual = new ManualCreator("resources/manual.tex");
        chooseDestination.setOnMouseClicked(e ->
        {
            File save = saveLocation.showSaveDialog(new Stage());
            manual.setPdfFilePath(save.getPath());
            String path = save.getPath();
            String fileName = save.getName();
            String[] fileBroken = fileName.split("\\.");
            manual.setOutputTexFile(new File("resources" + File.separator + fileBroken[0] + ".tex"));
        });

        nameManual = new TextField();
        nameManual.setPromptText("Enter manual title...");
        dialogGrid.add(nameManual, 0, 4);

        getDialogPane().getButtonTypes().add(ButtonType.OK);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    }

    /**
     * Sets up the actions of the dialog following submission.
     */
    void setupResultConverter()
    {
        setResultConverter(new Callback<ButtonType, Void>()
        {
            @Override
            public Void call(ButtonType buttonType)
            {
                if (buttonType == ButtonType.OK)
                {
                    ArrayList<Module> displayModules = new ArrayList<Module>(Main.MODULES_DISPLAYED);
                    Main.MODULES_DISPLAYED = new ArrayList<Module>(Main.MODULES_AVAILABLE);
                    /*SortDialogCreator sdc = new SortDialogCreator();*/
                    ArrayList<Module> needySection = new ArrayList<Module>();
                    ArrayList<Module> vanillaSection = new ArrayList<Module>();
                    if (splitNeedy.isSelected())
                    {//Separates active needy modules.
                        ArrayList<Module> temp = new ArrayList<Module>(Main.MODULES_DISPLAYED);
                        ArrayList<Integer> needyFilter = new ArrayList<Integer>();
                        needyFilter.add(2);
                        filterModules(needyFilter);
                        for (Module module : Main.MODULES_DISPLAYED)
                        {
                            if (module.isActive())
                            {
                                needySection.add(module);
                            }
                            temp.remove(module);
                        }
                        Main.MODULES_DISPLAYED = temp;
                        manual.setNeedyToEnd(true);
                    }
                    if (splitVanilla.isSelected())
                    {//Separates active vanilla modules.
                        ArrayList<Module> temp = new ArrayList<Module>(Main.MODULES_DISPLAYED);
                        ArrayList<Integer> vanillaFilter = new ArrayList<Integer>();
                        vanillaFilter.add(0);
                        filterModules(vanillaFilter);
                        for (Module module : Main.MODULES_DISPLAYED)
                        {
                            if (module.isActive())
                            {
                                vanillaSection.add(module);
                            }
                            temp.remove(module);
                        }
                        Main.MODULES_DISPLAYED = temp;
                        manual.setVanillaToEnd(true);
                    }
                    sortModules(0, false);
                    for (Module module : Main.MODULES_DISPLAYED)
                    {
                        if (module.isActive())
                        {
                            manual.addModule(module);
                        }
                    }
                    for (Module module : vanillaSection)
                        manual.addModule(module);
                    for (Module module : needySection)
                        manual.addModule(module);

                    if (nameManual.getText().equals(""))
                        manual.setManualName("My Manual");
                    else
                        manual.setManualName(nameManual.getText());
                    Main.MODULES_DISPLAYED = displayModules;
                    Main.clearModules();
                    Main.renderModules();
                    pd = new ProgressDialog();
                    pm = pd.getProgressManager();
                    //pd.createProgressBar();

                    pd.initBinding(pm.getProgressProperty());
                    pd.displayProgressBar();
                    Thread compileThread = new Thread(pBarTask);
                    compileThread.setDaemon(true);
                    compileThread.start();
                }
                return null;
            }
        });
    }

    /**
     * Task holding everything that will happen after the user submits the MakeManualDialog.
     */
    Task<Void> pBarTask = new Task<Void>()
    {//Task holding pdf creation and compilation code to be executed on a separate thread.
        @Override
        public Void call() throws Exception
        {
            try
            {
                ProcessBuilder builder = null;
                manual.writeManual(pm);
                if(manual.getPdfFilePath() == null)
                    builder = new ProcessBuilder("pdflatex", manual.getTexFilePath());
                else
                {
                    File pdf = new File(manual.getPdfFilePath());
                    String pdfDir = manual.getPdfFilePath().replace(pdf.getName(), "");
                    builder = new ProcessBuilder("pdflatex", "-output-directory",
                            pdfDir, manual.getTexFilePath());
                }

                builder.redirectErrorStream(true);
                Process pro = builder.start();
                BufferedReader inStrm = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                System.out.println("Should be rolling");
                String line = null;
                while ((line = inStrm.readLine()) != null)
                    System.out.print(line);
                File aux = new File(manual.getPdfFilePath().replace(".pdf", ".aux"));
                File log = new File(manual.getPdfFilePath().replace(".pdf", ".log"));
                aux.delete();//I don't think(?) I care about the result for now.
                log.delete();
                pm.setProgress(1);
                Platform.runLater(() -> pd.closeProgressBar());
            }
            catch (OutputIOException e)
            {//This exception will be thrown if there was an error writing to the tex file.
                Platform.runLater(new Runnable()
                {//Alert to the presented on the Application thread whenever possible.
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
            catch (IOException e)
            {//This exception will be thrown if there was an error compiling the pdf.
                Platform.runLater(new Runnable()
                {//Alert to be presented on the Application thread whenever next possible.
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
            return null;
        }
    };
}
