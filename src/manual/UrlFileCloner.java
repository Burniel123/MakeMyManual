package manual;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Temporarily clones a file from the KTANE repo so it can be used in a compiled manual.
 */
public class UrlFileCloner
{
    private String url = null;
    private URL fileUrl = null;
    private String destinationFilePath = null;

    /**
     * Creates an instance of UrlFileCloner, requiring the url to the file and the location it's being cloned to.
     * @param url - String containing the URL to the file to clone.
     * @param destinationFilePath - a String path to the location of the new file (including the .pdf).
     */
    public UrlFileCloner(String url, String destinationFilePath)
    {
        this.url = url;
        this.destinationFilePath = destinationFilePath;
    }

    /**
     * Clones a file using streams and Files.Copy.
     * @throws MalformedURLException - In the event that the URL is not suitably formed.
     * @throws IOException - In the event of an IO Error.
     * TODO: throw customised exceptions here!
     */
    public void cloneFile() throws MalformedURLException, IOException
    {
        fileUrl = new URL(url);
        InputStream in = fileUrl.openStream();
        Files.copy(in, Paths.get(destinationFilePath), StandardCopyOption.REPLACE_EXISTING);
    }
}
