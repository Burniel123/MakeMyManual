package display;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import manual.ManualException;

public class ExceptionAlert extends Alert
{
    public ExceptionAlert(ManualException exception)
    {
        super(AlertType.ERROR);
        setGraphic(null); //TODO: Use actual logo here?
        setTitle(exception.getExceptionTitle());
        setHeaderText(exception.getExceptionTitle());
        getDialogPane().getStylesheets().add(getClass().getResource("exceptionAlertStyle.css").
                toExternalForm());
        getDialogPane().getStyleClass().add("exceptionAlertStyle");

        Label causesLabel = new Label("Possible Causes:");
        causesLabel.setFont(new Font("Arial Bold", 16));
        String causes = "";
        for(String cause : exception.getPossibleCauses())
            causes += (cause + "\n");
        Label causesContent = new Label(causes);

        Label resolutionsLabel = new Label("\nPossible Resolutions:");
        resolutionsLabel.setFont(new Font("Arial Bold", 16));
        String resolutions = "";
        for(String resolution : exception.getPossibleResolutions())
            resolutions += (resolution + "\n");
        Label resolutionsContent = new Label(resolutions);

        Label contactLabel = new Label("\nIf issues persist, please contact Daniel Burton.s");
        contactLabel.setStyle("-fx-font-style: italic");
        VBox content = new VBox(3);
        content.getChildren().addAll(causesLabel, causesContent, resolutionsLabel, resolutionsContent, contactLabel);

        getDialogPane().setContent(content);
    }

    public ExceptionAlert(String title, String message)
    {
        super(AlertType.ERROR);
        setTitle(title);
        setHeaderText(title);
        setContentText(message);
    }
}
