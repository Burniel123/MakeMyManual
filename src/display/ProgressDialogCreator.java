package display;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * Manages the dialog which informs the user of the program's process in creating their manual.
 *
 * @author Daniel Burton
 */
public class ProgressDialogCreator
{
    public void displayProgressBar()
    {
        //Setting up the dialog window:
        final Dialog progressDialog = new Dialog();
        final VBox progressContent = new VBox(5);
        progressDialog.getDialogPane().setContent(progressContent);

        //Setting basic dialog properties:
        progressDialog.setTitle("Creating your manual...");
        progressDialog.setHeaderText("Your custom manual is being created.\nThis may take some time.");
        ProgressBar progressBar = new ProgressBar(0);
        progressContent.getChildren().add(progressBar);

        progressDialog.showAndWait();
    }
}
