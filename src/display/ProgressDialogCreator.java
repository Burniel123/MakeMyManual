package display;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.DoubleStringConverter;

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

    void createProgressBar()
    {//TODO: Consider making static? Maybe not because one run of the program may see multiple progress bars?
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
        {//TODO: document this properly.
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1)
            {
                progressBar.setProgress(pm.getProgress());
            }
        });
    }

    void initBinding(DoubleProperty dp)
    {
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(dp);
    }

    void displayProgressBar()
    {
        progressDialog.show();
    }

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

    ProgressBar getProgressBar()
    {
        return progressBar;
    }
}
