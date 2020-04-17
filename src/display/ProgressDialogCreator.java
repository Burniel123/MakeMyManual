package display;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 * Manages the dialog which informs the user of the program's process in creating their manual.
 *
 * @author Daniel Burton
 */
public class ProgressDialogCreator
{
    private final ProgressBar progressBar = new ProgressBar();
    private final Dialog progressDialog = new Dialog();
    private final ProgressManager pm = new ProgressManager();

    /**
     * Creates a dialog containing a progress bar, linked to a ProgressManager.
     */
    void createProgressBar()
    {
        //Setting up the dialog window:
        final VBox progressContent = new VBox(5);
        progressDialog.getDialogPane().setContent(progressContent);

        //Setting basic dialog properties:
        progressDialog.setTitle("Creating your manual...");
        progressDialog.setHeaderText("Your custom manual is being created.\nThis may take some time.");
        ProgressBar progressBar = new ProgressBar(0);
        progressContent.getChildren().add(progressBar);
        progressDialog.initModality(Modality.APPLICATION_MODAL);
        progressDialog.initStyle(StageStyle.UTILITY);

        pm.getProgressProperty().addListener(new ChangeListener<Number>()
        {//Responds to changes in the progress of the task being executed and updates the progress bar accordingly.
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number n, Number n1)
            {
                progressBar.setProgress(pm.getProgress());
            }
        });
    }

    /**
     * Adds binding properties to the progress bar.
     * @param dp - the property to bind the progress bar's progress to.
     */
    void initBinding(DoubleProperty dp)
    {
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(dp);
    }

    /**
     * Displays the progress bar dialog to the screen.
     */
    void displayProgressBar()
    {
        progressDialog.show();
    }

    /**
     * Closes the progress bar window, to be used once the task is complete.
     */
    void closeProgressBar()
    {
        //A "dummy" cancel button must be added before window can be closed.
        progressDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        progressDialog.close();
    }

    /**
     * Obtains the ProgressManager controlling a progress bar.
     * @return the ProgressManager which is managing this progress bar.
     */
    ProgressManager getProgressManager()
    {
        return pm;
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

    /**
     * Obtains the progress bar being displayed
     * @return the ProgressBar corresponding to this Dialog.
     */
    ProgressBar getProgressBar()
    {
        return progressBar;
    }
}
