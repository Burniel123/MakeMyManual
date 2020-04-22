package display;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
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
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        dialogGrid.setMinSize(350, 350);

        CheckBox subcategories = new CheckBox("Create a category per letter");
        subcategories.setMinWidth(300);
        dialogGrid.add(subcategories, 0, 0);
        splitNeedy = new CheckBox("Separate section for any needy modules at end");
        splitNeedy.setMinWidth(300);
        dialogGrid.add(splitNeedy, 0, 1);
        splitVanilla = new CheckBox("Separate section for any vanilla modules after mods");
        splitVanilla.setMinWidth(300);
        dialogGrid.add(splitVanilla, 0, 2);

        final Button chooseDestination = new Button("Choose manual location...");
        dialogGrid.add(chooseDestination, 0, 3);
        FileChooser saveLocation = new FileChooser();
        ExtensionFilter filter = new ExtensionFilter("Portable Document Format: .pdf", ".pdf");
        saveLocation.getExtensionFilters().add(filter);
        saveLocation.setTitle("Choose location to save manual");
        manual = new ManualCreator("resources/manual.tex");
        chooseDestination.setOnMouseClicked(e ->
        {
            File save = saveLocation.showSaveDialog(new Stage());
            if(save != null)
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
                    Main.ROOT_PANE.clearModules();
                    Main.ROOT_PANE.renderModules();
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
            File log = null;
            FileWriter logWriter = null;
            try
            {//Open log file for diagnostic purposes.
                log = new File("resources/ManualLog.txt");
                logWriter = new FileWriter(log);
                logWriter.write("***BEGIN PDF CREATION***");
            }
            catch(IOException e)
            {
                Platform.runLater(() ->
                {
                    ExceptionAlert ea = new ExceptionAlert("Error writing log file!",
                            "Do all working directories have appropriate permissions?");
                    ea.initOwner(getDialogPane().getScene().getWindow());
                    ea.showAndWait();
                    pd.closeProgressBar();
                });
                return null;
            }

            try
            {
                ProcessBuilder builder = null;
                manual.writeManual(pm, logWriter);
                boolean specified = false;
                if(manual.getPdfFilePath() == null)
                {
                    builder = new ProcessBuilder("pdflatex", manual.getTexFilePath());
                    manual.setPdfFilePath(System.getProperty("user.dir") + "/manual.pdf");
                }
                else
                {
                    File pdf = new File(manual.getPdfFilePath());
                    String pdfDir = manual.getPdfFilePath().replace(pdf.getName(), "");
                    builder = new ProcessBuilder("pdflatex", "-output-directory",
                            pdfDir, manual.getTexFilePath());
                    specified = true;
                }

                builder.redirectErrorStream(true);
                Process pro = builder.start();
                BufferedReader inStrm = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                System.out.println("Should be rolling");
                String line = null;
                logWriter.write("\n\n***BEGIN COMPILATION***");
                logWriter.write("\nOutput from pdflatex as follows: \n\n");
                while ((line = inStrm.readLine()) != null)
                    logWriter.write(line + "\n");
                File manualsDir = new File("resources/modules");
                for(File file : manualsDir.listFiles())
                    file.delete();
                File aux = new File(manual.getPdfFilePath().replace(".pdf", ".aux"));
                File pdflatexLog = new File(manual.getPdfFilePath().replace(".pdf", ".log"));
                aux.delete();//I don't think(?) I care about the result for now.
                pdflatexLog.delete();
                pm.setProgress(1);
                logWriter.write("\nManual success, located at " + manual.getPdfFilePath());
                logWriter.close();
                Platform.runLater(() -> pd.closeProgressBar());
                createCompletedAlert(specified);
            }
            catch (OutputIOException e)
            {//This exception will be thrown if there was an error writing to the tex file.
                Platform.runLater(new Runnable()
                {//Alert to the presented on the Application thread whenever possible.
                    @Override
                    public void run()
                    {
                        ExceptionAlert exceptionAlert = new ExceptionAlert(e);
                        exceptionAlert.showAndWait();
                        pd.initOwner(getDialogPane().getScene().getWindow());
                        pd.closeProgressBar();
                    }
                });
            }
            catch (IOException e)
            {//This exception will be thrown if there was an error compiling the pdf.
                Platform.runLater(() ->
                {//Alert to be presented on the Application thread whenever next possible.
                    ExceptionAlert exceptionAlert = new ExceptionAlert("Error compiling pdf!",
                            "Do all working directories have appropriate permissions?");
                    exceptionAlert.initOwner(getDialogPane().getScene().getWindow());
                    exceptionAlert.showAndWait();
                    pd.closeProgressBar();
                });
            }
            return null;
        }
    };

    /**
     * Creates and displays an alert to point the user to their newly-created manual.
     * @param specified - true if the user specified a path, false if the default path is being used.
     */
    private void createCompletedAlert(boolean specified)
    {
        Platform.runLater(() ->
        {//To make absolutely sure this method's content is run on the FX Application Thread.
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Manual created successfully!");
            successAlert.setHeaderText("Your custom manual, " + manual.getManualName() + ", has been created.");
            successAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            if(specified)
                successAlert.setContentText("Location: " + manual.getPdfFilePath());
            else
                successAlert.setContentText("Location: " + manual.getPdfFilePath());
            successAlert.setGraphic(null);
            //successAlert.initOwner(getDialogPane().getScene().getWindow());
            successAlert.showAndWait();
        });
    }
}
