package org.makemymanual.display;

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
public class ProgressDialog extends Dialog<Void>
{
    private final ProgressBar progressBar = new ProgressBar();
    //private final Dialog<Void> progressDialog = new Dialog<Void>();
    private final ProgressManager pm = new ProgressManager();

    ProgressDialog()
    {
        //Setting up the dialog window:
        final VBox progressContent = new VBox(5);
        getDialogPane().setContent(progressContent);
        getDialogPane().getStylesheets().add(getClass().getResource("/dialogStyle.css").toExternalForm());
        getDialogPane().getStyleClass().add("dialogStyle");

        //Setting basic dialog properties:
        setTitle("Creating your manual...");
        setHeaderText("Your custom manual is being created.\nThis may take some time.");
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMinWidth(300);
        progressContent.getChildren().add(progressBar);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);

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
        show();
    }

    /**
     * Closes the progress bar window, to be used once the task is complete.
     */
    void closeProgressBar()
    {
        //A "dummy" cancel button must be added before window can be closed.
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        close();
    }

    /**
     * Obtains the ProgressManager controlling a progress bar.
     * @return the ProgressManager which is managing this progress bar.
     */
    ProgressManager getProgressManager()
    {
        return pm;
    }
}
