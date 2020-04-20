package manual;

import display.ProgressManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Manages the content of, and eventually creates, a collated manual for the user's desired modules.
 *
 * @author Daniel Burton
 */
public class ManualCreator
{
    private File outputTexFile = null;
    private File pdfFile = null;
    private BufferedWriter writer = null;
    private boolean vanillaToEnd = false;
    private boolean needyToEnd = false;

    private ArrayList<Module> modules = new ArrayList<Module>();
    private String manualName = null; //TODO: check user has named their manual before creating!

    /**
     * Creates an instance of ManualCreator, requiring the path it will write to.
     * TODO: make sure this only ever contains a tex file!
     * @param texFilePath - path to a .tex file to write manual pages to.
     */
    public ManualCreator(String texFilePath)
    {
        outputTexFile = new File(texFilePath);
    }

    /**
     * Sets the file path to write the tex file to (and hence the pdf to).
     * @param texFilePath - a .tex file path to write to.
     */
    public void setOutputTexFile(File texFilePath)
    {
        outputTexFile = texFilePath;
    }

    /**
     * Obtain the path of the tex file so it can be compiled into a pdf.
     * @return a String representing the filepath of the tex file for this manual.
     */
    public String getTexFilePath()
    {
        return outputTexFile.getPath();
    }

    /**
     * Sets the eventual location the user would like the compiled pdf to be located.
     * @param pdfFilePath - a String path to the pdf's desired location.
     */
    public void setPdfFilePath(String pdfFilePath)
    {
        pdfFile = new File(pdfFilePath);
    }

    /**
     * Obtains the location the program is compiling the pdf to.
     * @return a String path of the eventual pdf location (null if user did not specify one).
     */
    public String getPdfFilePath()
    {
        if(pdfFile != null)
            return pdfFile.getPath();
        else
            return null;
    }

    /**
     * Adds a module to the list of modules to be included in this manual.
     * @param module - a Module object to add to the manual.
     */
    public void addModule(Module module)
    {
        modules.add(module);
    }

    public ArrayList<Module> getModules()
    {//TODO: properly document!
        return modules;
    }

    /**
     * Clears the list of modules.
     */
    public void clearModules()
    {
        modules.clear();
    }

    /**
     * Obtains the name of this manual.
     * @return this manual's name (title for the front page).
     */
    public String getManualName()
    {
        return manualName;
    }

    /**
     * Sets the name of the manual, to be printed on its cover.
     * @param manualName - user-defined String to name the manual.
     */
    public void setManualName(String manualName)
    {
        this.manualName = manualName;
    }

    /**
     * Sets whether or not a dedicated section is being made for vanillas at the end.
     * @param vanillaToEnd - true if a dedicated vanilla section is to be made, false otherwise.
     */
    public void setVanillaToEnd(boolean vanillaToEnd)
    {
        this.vanillaToEnd = vanillaToEnd;
    }

    /**
     * Sets whether or not a dedicated section is being made for needy modules at the end.
     * @param needyToEnd - true if a dedicated needy section is to be made, false otherwise.
     */
    public void setNeedyToEnd(boolean needyToEnd)
    {
        this.needyToEnd = needyToEnd;
    }

    /**
     * Writes the user's .tex manual, by creating a BufferedWriter, writing the preamble, writing each manual page,
     * and ending the document.
     * @throws OutputIOException in the event of an IOException.
     */
    public void writeManual(ProgressManager pm) throws OutputIOException
    {
        createWriter();
        writePreamble();

        for(int i = 0; i < modules.size(); i++)
        {
            if (vanillaToEnd && modules.get(i).getCategory() == 0)
            {
                beginVanillaSection();
                vanillaToEnd = false;
            }
            if (needyToEnd && modules.get(i).getCategory() == 2)
            {
                beginNeedySection();
                needyToEnd = false;
            }
            downloadFile(modules.get(i));
            writeManualPage(i);
            modules.get(i).deactivate();
            pm.setProgress((((double)i+1)/(double)modules.size())*0.7); //Update the progress bar.
        }
        endFile();
    }

    /**
     * Attempts to create a BufferedWriter to write the collated manual to the tex file.
     * @return a BufferedWriter set to write to the manual tex file.
     * @throws OutputIOException in the event of an IOException.
     */
    private void createWriter() throws OutputIOException
    {
        writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(outputTexFile));
        }
        catch(IOException e)
        {
            throw new OutputIOException(outputTexFile.getPath(), e, false);
        }
    }

    /**
     * Writes the preamble of the tex file, including the title of the manual, packages to use in the document, etc.
     * @throws OutputIOException in the event of an IOException.
     */
    private void writePreamble() throws OutputIOException
    {
        try
        {
            writer.write("\\documentclass{book}\n" +
                    "\\usepackage{graphicx}\n" +
                    "\\usepackage[margin=0.5in]{geometry}\n" +
                    "\\usepackage[final]{pdfpages}\n" +
                    "\\usepackage{bookmark}\n" +
                    "\\usepackage{hyperref}\n" +
                    "\\usepackage{cleveref}\n" +
                    "\\title{" + manualName +
                    "\\thanks{Produced with MakeMyManual}}" +
                    "\\author{}\n\\date{}\n" +
                    "\\begin{document}\n" +
                    "\\maketitle\n");
        }
        catch(IOException e)
        {
            System.out.println(e);
            throw new OutputIOException(outputTexFile.getPath(), e, true);
        }
    }

    /**
     * Writes a single manual page to the collated manual.
     * @param moduleIndex - which module in the list of modules to write.
     * @throws OutputIOException in the event of an IOException.
     */
    private void writeManualPage(int moduleIndex) throws OutputIOException
    {
        try
        {
            String manualPagePath = "modules/" + modules.get(moduleIndex).getModuleCode() + ".pdf";
            String moduleName = modules.get(moduleIndex).getModuleName();
            String moduleCodeName = modules.get(moduleIndex).getModuleCode();
            String path = editPath(manualPagePath);
            writer.write("\\label{pdf:" + moduleCodeName + "}\n" +
                    "\\pdfbookmark{" + moduleName + "}{pdf:" + moduleCodeName + "}\n" +
                    "\\includepdf[pages=-]{\"" + path + "\"}\n");
        }
        catch(IOException e)
        {
            throw new OutputIOException(outputTexFile.getPath(), e, true);
        }
    }

    /**
     * Writes the necessary ending commands to the tex file, most notably, ending the document section.
     * @throws OutputIOException - in the event of an IOException.
     */
    private void endFile() throws OutputIOException
    {
        try
        {
            writer.write("\\end{document}");
            writer.close();
        }
        catch(IOException e)
        {
            throw new OutputIOException(outputTexFile.getPath(), e, true);
        }
    }

    /**
     * Writes a bookmark to mark the start of a separated vanilla section.
     * @throws OutputIOException - in the event of an IOException.
     */
    private void beginVanillaSection() throws OutputIOException
    {
        try
        {
            writer.write("\\label{sec:vanilla}\n" +
                    "\\pdfbookmark{VANILLA}{sec:vanilla}\n");
        }
        catch(IOException e)
        {
            throw new OutputIOException(outputTexFile.getPath(), e, true);
        }
    }

    /**
     * Writes a bookmark to mark the start of a separated needy section.
     * @throws OutputIOException - in the event of an IOException.
     */
    private void beginNeedySection() throws OutputIOException
    {
        try
        {
            writer.write("\\label{sec:needy}\n" +
                    "\\pdfbookmark{NEEDY}{sec:needy}\n");
        }
        catch(IOException e)
        {
            throw new OutputIOException(outputTexFile.getPath(), e, true);
        }
    }

    /**
     * Edits a file path to make it LaTeX-friendly.
     * @param path - the path to edit.
     */
    private String editPath(String path)
    {
        String currentDir = System.getProperty("user.dir");
        path = currentDir + "/resources/" + path;
        String editedPath = "";
        for(int i = 0; i < path.length(); i++)
        {
            if(path.charAt(i) == '\\')
                editedPath += "/";
            else
                editedPath += path.charAt(i);
        }

        return editedPath;
    }

    /**
     * Downloads a manual page from the KTANE repository to a temporary file on the user's machine.
     * @param module - the module whose manual page should be downloaded.
     */
    private void downloadFile(Module module)
    {
        String destinationPath = "resources/modules/" + module.getModuleCode() + ".pdf";
        UrlFileCloner ufc = new UrlFileCloner(module.getManualLocation(), destinationPath);
        try
        {
            ufc.cloneFile();
        }
        catch(Exception ex)
        {
            System.out.println("ERROR\n" + ex);
            //TODO: PROPER EXCEPTION HANDLING HERE!
        }
    }
}
