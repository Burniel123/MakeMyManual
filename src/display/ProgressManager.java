package display;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Tracks the progress of a task such that a progress bar can be updated.
 *
 * @author Daniel Burton
 */
public class ProgressManager
{
    private final DoubleProperty progressProperty = new SimpleDoubleProperty();

    /**
     * Obtains the DoubleProperty associated with the progress of a task.
     * @return - DoubleProperty tracking this progress.
     */
    public DoubleProperty getProgressProperty()
    {
        return progressProperty;
    }

    /**
     * Sets the value of the DoubleProperty tracking the progress of a task.
     * @param progress - a double (snaps to between 0 and 1) to mark the progress of the task.
     */
    public void setProgress(double progress)
    {
        progressProperty.set(progress);
    }

    /**
     * Obtain the value of the DoubleProperty tracking the progress of a task, to update the progress bar.
     * @return - a double between 0 and 1 corresponding to the current progress of the task being tracked.
     */
    public double getProgress()
    {
        return progressProperty.get();
    }
}
