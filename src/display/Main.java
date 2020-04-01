package display;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import manual.InputIOException;
import manual.ManualListReader;

import java.util.ArrayList;

public class Main extends Application
{
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

        for(int i = 0; i < 50; i++)
        {
            final Region sampleRegion = new Region();
            sampleRegion.setStyle("-fx-background-color: red");
            sampleRegion.setMinSize(20, 20);
            modulesPane.getChildren().add(sampleRegion);
        }

        final Scene scene = new Scene(scrollableWindow);
        primaryStage.setTitle("MakeMyManual");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     *
     */
    @Override
    public void init()
    {
        ManualListReader reader = new ManualListReader();
        try
        {
            ArrayList<manual.Module> modulesAvailable = reader.readModuleList();
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    //Call a method to render the available mods on-screen.
                }
            });
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