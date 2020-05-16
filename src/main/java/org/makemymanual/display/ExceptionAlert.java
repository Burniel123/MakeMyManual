package org.makemymanual.display;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.makemymanual.manual.ManualException;

/**
 * Wraps a JavaFX Alert to present an error dialog to the user when an error is encountered.
 */
public class ExceptionAlert extends Alert
{
    /**
     * Constructor used to create an alert based on a custom exception specific to this application.
     * @param exception - some instance of ManualException - an error relating to MakeMyManual (eg pdf compilation).
     */
    public ExceptionAlert(ManualException exception)
    {
        super(AlertType.ERROR);//Start by creating a standard JavaFX error alert.
        setGraphic(null);
        setTitle(exception.getExceptionTitle());
        setHeaderText(exception.getExceptionTitle());

        //Adjust the styling of the alert:
        getDialogPane().getStylesheets().add(getClass().getResource
                ("/exceptionAlertStyle.css").toExternalForm());
        getDialogPane().getStyleClass().add("exceptionAlertStyle");

        //Create a list of possible causes, obtained from the exception argument.
        Label causesLabel = new Label("Possible Causes:");
        causesLabel.setFont(new Font("Arial Bold", 16));
        String causes = "";
        for(String cause : exception.getPossibleCauses())
            causes += (cause + "\n");
        Label causesContent = new Label(causes);

        //Create a list of possible resolutions, obtained from the exception argument.
        Label resolutionsLabel = new Label("\nPossible Resolutions:");
        resolutionsLabel.setFont(new Font("Arial Bold", 16));
        String resolutions = "";
        for(String resolution : exception.getPossibleResolutions())
            resolutions += (resolution + "\n");
        Label resolutionsContent = new Label(resolutions);

        //Create a note to contact the software owner if unexpected issues persist.
        Label contactLabel = new Label("\nIf issues persist, please contact Daniel Burton.");
        contactLabel.setStyle("-fx-font-style: italic");
        VBox content = new VBox(3);
        content.getChildren().addAll(causesLabel, causesContent, resolutionsLabel, resolutionsContent, contactLabel);

        getDialogPane().setContent(content);
    }

    /**
     * Constructor used to create an alert based on a generic Java exception which has not been modelled by the system.
     * @param title - a String to describe the exception.
     * @param message - a String to explain causes/resolutions to the user.
     */
    public ExceptionAlert(String title, String message)
    {
        super(AlertType.ERROR);
        setTitle(title);
        setHeaderText(title);
        setContentText(message);
    }
}
