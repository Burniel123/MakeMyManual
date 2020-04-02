package display;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import manual.InputIOException;
import manual.ManualListReader;

import java.util.ArrayList;

public class Main extends Application
{
    private final VBox MAIN_PANE = new VBox(3);
    private final String DEFAULT_STYLE = "-fx-background-color: darkred";

    private ArrayList<manual.Module> MODULES_AVAILABLE = new ArrayList<manual.Module>();

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        /*Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));*/
        final FlowPane modulesPane = new FlowPane(2,2);
        final ScrollPane scrollableWindow = new ScrollPane(modulesPane);
        scrollableWindow.setFitToHeight(true);
        scrollableWindow.setFitToWidth(true);

        final HBox topMenu = new HBox(2);
        final TextField searchBarInput = new TextField("Search for mods...");
        final Button searchBarSubmit = new Button("Search!");
        final Separator searchSeparator = new Separator();
        searchSeparator.setOrientation(Orientation.VERTICAL);
        final Button filterMenu = new Button("Filter");
        final Button presetsButton = new Button("Presets");
        topMenu.getChildren().addAll(searchBarInput, searchBarSubmit, searchSeparator,
                filterMenu, presetsButton);

        final HBox numSelectedBar = new HBox(2);
        final Label numSelected = new Label("Modules Selected: 0/98");
        numSelectedBar.getChildren().add(numSelected);
        MAIN_PANE.getChildren().addAll(topMenu, numSelectedBar);

        for(int i = 0; i < 200; i++)
        {
            final Region sampleRegion = new Region();
            sampleRegion.setStyle(DEFAULT_STYLE);
            sampleRegion.setMinSize(20, 20);
            modulesPane.getChildren().add(sampleRegion);
        }

        MAIN_PANE.getChildren().add(scrollableWindow);
        final Scene scene = new Scene(MAIN_PANE);
        primaryStage.setTitle("MakeMyManual");
        primaryStage.setScene(scene);
        primaryStage.show();
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
/*            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    //Call a method to render the available mods on-screen.
                }
            });*/
        }
        catch(InputIOException e)
        {
            //Call a method to pop up an exception alert.
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