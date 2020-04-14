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
public class MakeManualDialogCreator
{
    private ManualCreator manual = null;
    private ProgressDialogCreator pdc = null;
    //private Task pBarTask = pBarTaskCreator();

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

        TextField nameManual = new TextField();
        nameManual.setPromptText("Enter manual title...");
        dialogGrid.add(nameManual, 0, 4);

        makeManualDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        makeManualDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        makeManualDialog.setResultConverter(new Callback<ButtonType, Void>()
        {
            @Override
            public Void call(ButtonType buttonType) {
                if (buttonType == ButtonType.OK)
                {
                    ArrayList<Module> displayModules = new ArrayList<Module>(Main.MODULES_DISPLAYED);
                    Main.MODULES_DISPLAYED = new ArrayList<Module>(Main.MODULES_AVAILABLE);
                    SortDialogCreator sdc = new SortDialogCreator();
                    ArrayList<Module> needySection = new ArrayList<Module>();
                    ArrayList<Module> vanillaSection = new ArrayList<Module>();
                    if (splitNeedy.isSelected()) {//Separates active needy modules.
                        ArrayList<Module> temp = new ArrayList<Module>(Main.MODULES_DISPLAYED);
                        ArrayList<Integer> needyFilter = new ArrayList<Integer>();
                        needyFilter.add(2);
                        sdc.filterModules(needyFilter);
                        for (Module module : Main.MODULES_DISPLAYED) {
                            if (module.isActive())
                            {
                                needySection.add(module);
                              //  downloadFile(module);
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
                        sdc.filterModules(vanillaFilter);
                        for (Module module : Main.MODULES_DISPLAYED) {
                            if (module.isActive())
                            {
                                vanillaSection.add(module);
                              //  downloadFile(module);
                            }
                            temp.remove(module);
                        }
                        Main.MODULES_DISPLAYED = temp;
                        manual.setVanillaToEnd(true);
                    }
                    sdc.sortModules(0, false);
                    for (Module module : Main.MODULES_DISPLAYED) {
                        if (module.isActive())
                        {
                            manual.addModule(module);
                           // downloadFile(module);
                        }
                    }
                    for (Module module : vanillaSection)
                        manual.addModule(module);
                    for (Module module : needySection)
                        manual.addModule(module);

                    if(nameManual.getText().equals(""))
                        manual.setManualName("My Manual");
                    else
                        manual.setManualName(nameManual.getText());
                    Main.MODULES_DISPLAYED = displayModules;
                    Main.clearModules();
                    Main.renderModules();
                    pdc = new ProgressDialogCreator();
                    pdc.createProgressBar();
                    ProgressManager pm = pdc.getProgressManager();
                    /*Runnable compile = new Runnable()
                    {//tex file creation and compilation will happen in a separate thread as it takes ages and is risky.
                        @Override
                        public void run()
                        {
                            try
                            {
                                manual.writeManual();
                                File pdf = new File(manual.getPdfFilePath());
                                String pdfDir = manual.getPdfFilePath().replace(pdf.getName(), "");
                                ProcessBuilder builder = new ProcessBuilder("pdflatex","-output-directory",
                                        pdfDir, manual.getTexFilePath());
                                builder.redirectErrorStream(true);
                                Process pro = builder.start();
                                BufferedReader inStrm = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                                System.out.println("Should be rolling");
                                String line = null;
                                while ((line = inStrm.readLine()) != null)
                                    System.out.print(line);
                            }
                            catch (OutputIOException e)
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
                            catch (IOException e)
                            {//This exception will be thrown if there was an error compiling the pdf.
                                Platform.runLater(new Runnable()
                                {
                                    @Override
                                    public void run() {
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
                    };*/
                    Task<Void> pBarTask = new Task<Void>()
                    {//TODO: document this properly.
                        @Override
                        public Void call() throws Exception
                        {
                            try
                            {
                                int numModules = manual.getModules().size();

                                //updateProgress(10,10);
                                manual.writeManual(pm);
                                //pdc.getProgressBar().progressProperty().unbind();
                                //pdc.getProgressBar().progressProperty().bind(pm.getProgressProperty());
                                File pdf = new File(manual.getPdfFilePath());
                                String pdfDir = manual.getPdfFilePath().replace(pdf.getName(), "");
                                ProcessBuilder builder = new ProcessBuilder("pdflatex","-output-directory",
                                        pdfDir, manual.getTexFilePath());
                                builder.redirectErrorStream(true);
                                Process pro = builder.start();
                                BufferedReader inStrm = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                                System.out.println("Should be rolling");
                                String line = null;
                                while ((line = inStrm.readLine()) != null)
                                    System.out.print(line);
                                pm.setProgress(1);
                                /*pm.setProgress(1.0);
                                pdc.getProgressBar().progressProperty().unbind();
                                pdc.getProgressBar().progressProperty().bind(pm.getProgressProperty());*/
                            }
                            catch (OutputIOException e)
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
                            catch (IOException e)
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
                            return null;
                        }
                    };
/*
                    pdc.getProgressBar().progressProperty().unbind();
                    pdc.getProgressBar().progressProperty().bind(pBarTask.progressProperty());
*/
                    pdc.initBinding(pm.getProgressProperty());
                    pdc.displayProgressBar();
                    Thread compileThread = new Thread(pBarTask);
                    compileThread.setDaemon(true);
                    compileThread.start();
                }

                return null;
            }
        });
        makeManualDialog.showAndWait();
    }

    /*private*/
}
