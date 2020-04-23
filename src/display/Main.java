package display;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import manual.InputIOException;
import manual.ManualListReader;
import manual.Module;

import java.util.ArrayList;

/**
 * Main application class for MakeMyManual.
 *
 * @author Daniel Burton
 */
public class Main extends Application implements Sortable
{
    static RootPane ROOT_PANE = null;
    static final Image DEFAULT_ICON = new Image("file:resources/icon.png");

    private static boolean exceptionOnBoot = false;
    private static InputIOException bootException = null;
    static int numSelected = 0;

    static ArrayList<Module> MODULES_AVAILABLE = new ArrayList<Module>();//Please don't change after init.
    static ArrayList<Module> MODULES_DISPLAYED = new ArrayList<Module>();

    /**
     * Starts the application, setting up and displaying the stage.
     * @param primaryStage - the main stage for the application.
     */
    @Override
    public void start(Stage primaryStage)
    {
        ROOT_PANE = new RootPane();

        final Scene scene = new Scene(ROOT_PANE);
        primaryStage.setTitle("MakeMyManual");
        primaryStage.setScene(scene);
        primaryStage.setWidth(750);
        primaryStage.setHeight(500);
        primaryStage.getIcons().add(DEFAULT_ICON);

        if (exceptionOnBoot)
        {
            ExceptionAlert exceptionAlert = new ExceptionAlert(bootException);
            exceptionAlert.initOwner(primaryStage);
            exceptionAlert.showAndWait();
            Platform.exit();
        }
        else
            primaryStage.show();
    }

    /**
     * Filters modules displayed by a provided search term.
     * @param searchTerm - term to search by - only modules whose names contain this term will be featured.
     */
    static void searchModules(String searchTerm)
    {
            ArrayList<Module> temp = new ArrayList<Module>(MODULES_DISPLAYED);
            for(Module module : temp)
            {
                if(!module.getModuleName().toLowerCase().contains(searchTerm.toLowerCase()))
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
            MODULES_AVAILABLE = reader.readModuleList();
            MODULES_DISPLAYED = new ArrayList<Module>(MODULES_AVAILABLE);
            /*SortDialogCreator sdc = new SortDialogCreator();*/
            sortModules(0, false);
        }
        catch(InputIOException e)
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