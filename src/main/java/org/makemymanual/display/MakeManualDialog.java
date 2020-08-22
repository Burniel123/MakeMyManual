package org.makemymanual.display;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.makemymanual.manual.*;
import org.makemymanual.manual.Module;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Holds functionality relating to the creation and operation of a dialog to create a manual for the selected modules.
 * 
 * @author Daniel Burton
 */
public class MakeManualDialog extends Dialog<Void> implements Sortable
{
    private ManualCreator manual = null;
    private ProgressDialog pd = null;
    private ProgressManager pm = null;

    private CheckBox splitVanilla = null;
    private CheckBox splitNeedy = null;
    private CheckBox subcategories = null;
    private TextField nameManual = null;


    /**
     * Creates the dialog to generate a manual, to be rendered by the event handler associated with the create button.
     */
    MakeManualDialog()
    {
        //Configure the dialog pane to hold manual configuration options:
        DialogPane mmDialogPane = new DialogPane();
        GridPane dialogGrid = new GridPane();
        dialogGrid.setHgap(5);
        dialogGrid.setVgap(5);
        mmDialogPane.setContent(dialogGrid);
        setDialogPane(mmDialogPane);
        setTitle("Export Manual");
        setHeaderText("Export Manual");
        setContentText("Choose your preferences below and hit OK.");
        mmDialogPane.setMinHeight(Region.USE_PREF_SIZE);
        dialogGrid.setMinSize(350, 350);

        //Create checkboxes for optional features:
        subcategories = new CheckBox("Create a category per letter");
        subcategories.setMinWidth(300);
        dialogGrid.add(subcategories, 0, 0);
        splitNeedy = new CheckBox("Separate section for any needy modules at end");
        splitNeedy.setMinWidth(300);
        dialogGrid.add(splitNeedy, 0, 1);
        splitVanilla = new CheckBox("Separate section for any vanilla modules after mods");
        splitVanilla.setMinWidth(300);
        dialogGrid.add(splitVanilla, 0, 2);

        //Create options to choose target file for the compiled manual.
        final Button chooseDestination = new Button("Choose manual location...");
        dialogGrid.add(chooseDestination, 0, 3);
        FileChooser saveLocation = new FileChooser();
        ExtensionFilter filter = new ExtensionFilter("Portable Document Format: .pdf", "*.pdf");
        saveLocation.getExtensionFilters().add(filter);
        saveLocation.setTitle("Choose location to save manual");
        manual = new ManualCreator("manual.tex");
        chooseDestination.setOnMouseClicked(e ->
        {//Launch an OS-managed file chooser to choose where the compiled pdf should be located.
            File save = saveLocation.showSaveDialog(new Stage());
            if(save != null)
            {
                manual.setPdfFilePath(save.getPath());
                String fileName = save.getName();
                String[] fileBroken = fileName.split("\\.");
                String toUse = fileName;
                if(fileBroken[fileBroken.length-1].equals("pdf"))
                    toUse = fileName.substring(0, fileName.lastIndexOf("." + fileBroken[fileBroken.length-1]));
                else
                    manual.setPdfFilePath(save.getPath() + ".pdf");
                File texDir = new File("tex");
                texDir.mkdir(); //Double check there's a directory available to avoid NoSuchFile exceptions.
                manual.setOutputTexFile(new File("tex" + File.separator + toUse + ".tex"));
            }
        });

        //Create box to name the manual (only affects the front page).
        nameManual = new TextField();
        nameManual.setPromptText("Enter manual title...");
        dialogGrid.add(nameManual, 0, 4);
        mmDialogPane.getStylesheets().add(getClass().getResource("/dialogStyle.css").toExternalForm());
        mmDialogPane.getStyleClass().add("dialogStyle");
        //Add buttons to the bottom of the pane:
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
                    ArrayList<Module> needySection = new ArrayList<Module>();
                    ArrayList<Module> vanillaSection = new ArrayList<Module>();
                    manual.setAlphaSubs(subcategories.isSelected());
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
                    ArrayList<String> dependencyModules = new ArrayList<String>();
                    for(Module dependentModule : Main.MODULES_DISPLAYED)
                    {
                        if(dependentModule.isActive() && Main.MODULE_DEPENDENCIES.get(dependentModule.getModuleCode()) != null)
                            dependencyModules.addAll(Arrays.asList(Main.MODULE_DEPENDENCIES.get(dependentModule.getModuleCode())));
                    }
                    for (Module module : Main.MODULES_DISPLAYED)
                    {
                        if (module.isActive() || (!dependencyModules.isEmpty() && dependencyModules.contains(module.getModuleCode())))
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
                log = new File("ManualLog.txt");
                logWriter = new FileWriter(log);
                logWriter.write("***BEGIN PDF CREATION***");
            }
            catch(IOException e)
            {
                resetWhenCompleteOrException();
                Platform.runLater(() ->
                {
                    OutputIOException oe = new OutputIOException();
                    oe.addPossibleCause("Desired log file directory has insufficient permissions.");
                    oe.addPossibleCause("Existing log file is currently open.");
                    oe.addPossibleResolution("Close any log files currently open.");
                    oe.addPossibleResolution("Reinstall the application in a directory you can write to.");
                    ExceptionAlert ea = new ExceptionAlert(oe);
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

                Path aux = Paths.get(manual.getPdfFilePath().replace(".pdf", ".aux"));
                Path pdflatexLog = Paths.get(manual.getPdfFilePath().replace(".pdf", ".log"));
                Files.deleteIfExists(aux);
                Files.deleteIfExists(pdflatexLog);
                builder.redirectErrorStream(true);
                Process pro = builder.start();
                BufferedReader inStrm = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                String line = null;
                logWriter.write("\n\n***BEGIN COMPILATION***");
                logWriter.write("\nOutput from pdflatex as follows: \n\n");
                logWriter.write("[Some known irrelevant warnings removed]\n");
                int lineNum = 0;
                while ((line = inStrm.readLine()) != null)
                {
                    if(line.contains("I can't write on file"))
                    {
                        OutputIOException oioe = new OutputIOException();
                        oioe.addPossibleCause("Destination pdf file is open.");
                        oioe.addPossibleResolution("Close the pdf and restart the application.");
                        oioe.addPossibleResolution("Choose a different destination for your manual.");
                        throw oioe;
                    }
                    if(!(line.contains("ProcSets") || line.contains("invalid other resource")))
                    {//Ignore certain unavoidable pdf compilation warnings which are of no help.
                        lineNum++;
                        logWriter.write(line + "\n");
                        float proportion = (float)lineNum/(Main.MODULES_DISPLAYED.size())*0.3f;
                        pm.setProgress(0.7 + proportion);
                    }
                }
                File manualsDir = new File("manuals");
                for(File file : manualsDir.listFiles())
                    file.delete();//To avoid clogging the user's hard drive, delete all downloaded manual pages.
                pm.setProgress(1);
                Files.deleteIfExists(aux);
                Files.deleteIfExists(pdflatexLog);
                logWriter.write("\nManual success, located at " + manual.getPdfFilePath());
                logWriter.close();
                Platform.runLater(() -> pd.closeProgressBar());
                createCompletedAlert(specified);
            }
            catch (OutputIOException | FileDownloadException e)
            {//These exceptions will be thrown if there was an error downloading manuals or writing to the tex file.
                displayExceptionAlert(e);
            }
            catch (IOException e)
            {//This exception will be thrown if there was an error compiling the pdf.
                resetWhenCompleteOrException();
                Platform.runLater(() ->
                {//Alert to be presented on the Application thread whenever next possible.
                    OutputIOException oe = new OutputIOException();
                    oe.addPossibleCause("A corrupted file is preventing pdf compilation.");
                    oe.addPossibleCause("Application pre-requisites have not been met.");
                    oe.addPossibleCause("Program does not have sufficient permissions to create your manual.");
                    oe.addPossibleResolution("Ensure you have all required pre-requisites installed.");
                    oe.addPossibleResolution("Delete any existing auxiliary and log files in the application's directory.");
                    oe.addPossibleResolution("Reinstall the program in a directory with permissions.");
                    ExceptionAlert exceptionAlert = new ExceptionAlert(oe);
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
        resetWhenCompleteOrException();
        Platform.runLater(() ->
        {//To make absolutely sure this method's content is run on the FX Application Thread.
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.getDialogPane().getStylesheets().add(getClass().getResource("/dialogStyle.css").
                    toExternalForm());
            successAlert.getDialogPane().getStyleClass().add("dialogStyle");
            successAlert.setTitle("Manual created successfully!");
            successAlert.setHeaderText("Your custom manual, " + manual.getManualName() + ", has been created.");
            successAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            if(specified)
                successAlert.setContentText("Location: " + manual.getPdfFilePath());
            else
                successAlert.setContentText("Location: " + manual.getPdfFilePath());
            successAlert.setGraphic(null);
            successAlert.showAndWait();
        });
    }

    /**
     * Initialises and displays an instance of ExceptionAlert when an exception has been encountered
     * while making a custom manual.
     * @param e - the exception that was thrown.
     */
    private void displayExceptionAlert(ManualException e)
    {
        resetWhenCompleteOrException();
        Platform.runLater(new Runnable()
        {//Alert to the presented on the Application thread whenever possible.
            @Override
            public void run()
            {
                ExceptionAlert exceptionAlert = new ExceptionAlert(e);
                exceptionAlert.showAndWait();
                pd.closeProgressBar();
            }
        });
    }

    /**
     * Resets the display and modules selected for when manual creation has terminated,
     * either because of a successful compilation or an exception being thrown.
     */
    private void resetWhenCompleteOrException()
    {
        Platform.runLater(() ->
        {
            Main.ROOT_PANE.clearModules();
            Main.ROOT_PANE.renderModules();
            Main.numSelectedProperty.set(0);
        });
    }
}
