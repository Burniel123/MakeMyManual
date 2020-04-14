package display;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class ProgressManager
{
    private final DoubleProperty progressProperty = new SimpleDoubleProperty();

    public DoubleProperty getProgressProperty()
    {
        return progressProperty;
    }

    public void setProgress(double progress)
    {
        progressProperty.set(progress);
    }

    public double getProgress()
    {
        return progressProperty.get();
    }
}
