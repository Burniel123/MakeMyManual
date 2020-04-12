package display;

import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * Manages the dialog which informs the user of the program's process in creating their manual.
 *
 * @author Daniel Burton
 */
public class ProgressDialogCreator
{
    private ProgressBar progressBar = new ProgressBar(0);

    void displayProgressBar()
    {//TODO: Consider making static? Maybe not because one run of the program may see multiple progress bars?
        //Setting up the dialog window:
        final Dialog progressDialog = new Dialog();
        final VBox progressContent = new VBox(5);
        progressDialog.getDialogPane().setContent(progressContent);

        //Setting basic dialog properties:
        progressDialog.setTitle("Creating your manual...");
        progressDialog.setHeaderText("Your custom manual is being created.\nThis may take some time.");
        ProgressBar progressBar = new ProgressBar(0);
        progressContent.getChildren().add(progressBar);

        progressDialog.show();
    }

    /**
     * Updates the progress to be shown on the progress bar, in accordance with how far through compilation we are.
     * @param progress - double between 0 and 1 indicating what proportion of the way through compilation
     *                 the program is.
     */
    void setProgress(double progress)
    {//TODO: Maybe throw here?
        if(progress >= 0 && progress <= 1)
            progressBar.setProgress(progress);
    }
}
