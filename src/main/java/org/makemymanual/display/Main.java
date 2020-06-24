package org.makemymanual.display;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.makemymanual.manual.InputIOException;
import org.makemymanual.manual.ManualListReader;
import org.makemymanual.manual.Module;
import org.makemymanual.manual.UrlFileCloner;

import java.io.*;
import java.util.ArrayList;

/**
 * Main application class for MakeMyManual.
 *
 * @author Daniel Burton
 */
public class Main extends Application implements Sortable {
    static RootPane ROOT_PANE = null;
    static final Image DEFAULT_ICON = new Image(Main.class.getResource("/icon.png").toExternalForm());
    private static final String CONFIG_FILE = "https://raw.githubusercontent.com/Burniel123/MakeMyManual/master/src/main/resources/modules-config-details.txt";

    private static boolean exceptionOnBoot = false;
    private static InputIOException bootException = null;
    static IntegerProperty numSelectedProperty = new SimpleIntegerProperty(0);
    static int numSelected = 0;

    static ArrayList<Module> MODULES_AVAILABLE = new ArrayList<Module>();//Please don't change after init.
    static ArrayList<Module> MODULES_DISPLAYED = new ArrayList<Module>();

    /**
     * Starts the application, setting up and displaying the stage.
     *
     * @param primaryStage - the main stage for the application.
     */
    @Override
    public void start(Stage primaryStage)
    {
        //Outsource creating the main display of the application to the RootPane class:
        ROOT_PANE = new RootPane();

        //Build the scene and stage:
        final Scene scene = new Scene(ROOT_PANE);
        primaryStage.setTitle("MakeMyManual");
        primaryStage.setScene(scene);
        primaryStage.setWidth(750);
        primaryStage.setHeight(500);
        primaryStage.getIcons().add(DEFAULT_ICON);

        if (exceptionOnBoot)
        {//If an exception was encountered on init, don't show the main display and show an error dialog instead.
            ExceptionAlert exceptionAlert = new ExceptionAlert(bootException);
            exceptionAlert.initOwner(primaryStage);
            exceptionAlert.showAndWait();
            Platform.exit();
        } else //If initialised successfully, show the main stage.
            primaryStage.show();
    }

    /**
     * Filters modules displayed by a provided search term.
     *
     * @param searchTerm - term to search by - only modules whose names contain this term will be featured.
     */
    static void searchModules(String searchTerm)
    {
        ArrayList<Module> temp = new ArrayList<Module>(MODULES_DISPLAYED);
        for (Module module : temp) {
            if (!module.getModuleName().toLowerCase().contains(searchTerm.toLowerCase()))
                MODULES_DISPLAYED.remove(module);
        }
        ROOT_PANE.clearModules();
        ROOT_PANE.renderModules();
        MODULES_DISPLAYED = temp;
    }

    /**
     * Initialises the Application by reading the module list file.
     */
    @Override
    public void init()
    {
        ManualListReader reader = new ManualListReader();
        try
        {
            updateLatexPackages();
            updateModules();
            MODULES_AVAILABLE = reader.readModuleList();
            MODULES_DISPLAYED = new ArrayList<Module>(MODULES_AVAILABLE);
            sortModules(0, false);
        }
        catch (InputIOException e)
        {
            Platform.runLater(new Runnable()
            {//If an exception was thrown, flag that an exception should be thrown.
                @Override
                public void run()
                {
                    exceptionOnBoot = true;
                    bootException = e;
                }
            });
        }
        catch (IOException e)
        {
            Platform.runLater(new Runnable()
            {//If an exception was thrown, flag that an exception should be thrown.
                @Override
                public void run() {
                    InputIOException iioe = new InputIOException();
                    iioe.addPossibleCause("Package/module maintainer moved, interrupted, or corrupted");
                    iioe.addPossibleResolution("Reboot the application");
                    iioe.addPossibleResolution("Reinstall the application");
                    exceptionOnBoot = true;
                    bootException = iioe;
                }
            });
        }
    }

    /**
     * To be called at the init() stage, checks all required LaTeX packages are installed and up-to-date
     * on startup.
     *
     * @throws IOException - in case of file reading/writing error when writing and compiling dummy LaTeX.
     */
    private void updateLatexPackages() throws IOException
    {
        //Obtain the dummy code to write as a stream:
        InputStream packageMaint = getClass().getResourceAsStream("/packagemaintainer.tex");
        File maintainerFile = new File("packmaint.tex");
        BufferedWriter fw = new BufferedWriter(new FileWriter(maintainerFile));
        String maintLine = null;
        BufferedReader packageReader = new BufferedReader(new InputStreamReader(packageMaint));
        while ((maintLine = packageReader.readLine()) != null)//Write the dummy code to a TeX file.
            fw.write(maintLine + "\n");
        fw.close();
        ProcessBuilder pb = new ProcessBuilder("pdflatex", maintainerFile.getPath());
        pb.redirectErrorStream(true);
        Process pro = pb.start();//Compile the dummy LaTeX to force-update packages.
        System.out.println("Successfully maintained packages");
    }

    /**
     * To be called at the init() stage, downloads the latest module configurations from the repository.
     *
     * @throws IOException - in the event of a standard input error.
     */
    private void updateModules() throws IOException
    {
        UrlFileCloner ufc = new UrlFileCloner(CONFIG_FILE, "module-config-details.txt");
        ufc.cloneFile();
    }

    /**
     * Main method - only purpose currently is to launch the FX Application.
     * @param args - Command line arguments.
     */
    public static void main(String[] args)
    {
        launch(args);
    }

}