package org.makemymanual.manual;

import org.makemymanual.display.ProgressManager;
import org.makemymanual.display.Sortable;

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
public class ManualCreator implements Sortable
{
    private File outputTexFile = null;
    private File pdfFile = null;
    private BufferedWriter writer = null;
    private boolean vanillaToEnd = false;
    private boolean needyToEnd = false;
    private boolean alphaSubs = false;

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

    /**
     * Obtains the list of modules being made into a manual.
     * @return ArrayList of Modules currently in the creator.
     */
    public ArrayList<Module> getModules()
    {
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
     * Sets whether or not a dedicated section is being made for each letter.
     * @param alphaSubs - true if bookmarks are to be subcategorised by letter.
     */
    public void setAlphaSubs(boolean alphaSubs)
    {
        this.alphaSubs = alphaSubs;
    }

    /**
     * Writes the user's .tex manual, by creating a BufferedWriter, writing the preamble, writing each manual page,
     * and ending the document.
     * @param pm - ProgressManager to report progress to the progress bar.
     * @param logWriter - FileWriter to write updates to the log file.
     * @throws OutputIOException in the event of an IOException.
     */
    public void writeManual(ProgressManager pm, FileWriter logWriter) throws OutputIOException, IOException
    {
        createWriter();
        writePreamble();
        char firstChar = ' ';
        if(!modules.isEmpty())
            firstChar = modules.get(0).getModuleName().charAt(0);
        char currentChar = Character.toUpperCase(firstChar);

        if(alphaSubs && (firstChar >= 48 && firstChar <= 57))
            writer.write("\\label{alpha:09}\n" +
                    "\\pdfbookmark[0]{0-9}{alpha:09}\n");
        else if(alphaSubs)
            writer.write("\\label{alpha:" + currentChar + "}\n" +
                    "\\pdfbookmark[0]{" + currentChar + "}{alpha:" + currentChar + "}\n");

        for(int i = 0; i < modules.size(); i++)
        {
            if(alphaSubs && removeThe(modules.get(i).getModuleName()).charAt(0) != currentChar
            && modules.get(i).getModuleName().charAt(0) > 57 && modules.get(i).getCategory() == 1)
            {
                currentChar = removeThe(modules.get(i).getModuleName()).charAt(0);
                writer.write("\\label{alpha:" + currentChar + "}\n" +
                        "\\pdfbookmark[0]{" + currentChar + "}{alpha:" + currentChar + "}\n");
            }
            if (vanillaToEnd && modules.get(i).getCategory() == 0)
            {
                //beginVanillaSection();
                writer.write("\\label{vanilla}\n" +
                        "\\pdfbookmark[0]{Vanilla}{vanilla}\n");
                vanillaToEnd = false;
            }
            if (needyToEnd && modules.get(i).getCategory() == 2)
            {
                //beginNeedySection();
                writer.write("\\label{needy}\n" +
                        "\\pdfbookmark[0]{Needy}{needy}\n");
                needyToEnd = false;
            }
            downloadFile(modules.get(i));
            logWriter.write("\nSuccessfully downloaded manual for: " + modules.get(i).getModuleName().toUpperCase());
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
            throwOutIO();
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
                    "\\pdfminorversion=7\n" +
                    "\\begin{document}\n" +
                    "\\maketitle\n");
        }
        catch(IOException e)
        {
            throwOutIO();
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
            String manualPagePath = "/manuals/" + modules.get(moduleIndex).getModuleCode() + ".pdf";
            String moduleName = modules.get(moduleIndex).getModuleName();
            String moduleCodeName = modules.get(moduleIndex).getModuleCode();
            String path = editPath(manualPagePath);
            writer.write("\\label{pdf:" + moduleCodeName + "}\n");
            writer.write("\\pdfbookmark[1]{" + moduleName + "}{pdf:" + moduleCodeName + "}\n");
            writer.write("\\includepdf[pages=-]{\"" + path + "\"}\n");
        }
        catch(IOException e)
        {
            throwOutIO();
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
            throwOutIO();
        }
    }

/*    *//**
     * Writes a bookmark to mark the start of a separated vanilla section.
     * @throws OutputIOException - in the event of an IOException.
     *//*
    private void beginVanillaSection() throws OutputIOException
    {
        try
        {
            writer.write("\\label{sec:vanilla}\n" +
                    "\\pdfbookmark{VANILLA}{sec:vanilla}\n");
        }
        catch(IOException e)
        {
            throwOutIO();
        }
    }*/

/*    *//**
     * Writes a bookmark to mark the start of a separated needy section.
     * @throws OutputIOException - in the event of an IOException.
     *//*
    private void beginNeedySection() throws OutputIOException
    {
        try
        {
            writer.write("\\label{sec:needy}\n" +
                    "\\pdfbookmark{NEEDY}{sec:needy}\n");
        }
        catch(IOException e)
        {
            throwOutIO();
        }
    }*/

    /**
     * Edits a file path to make it LaTeX-friendly.
     * @param path - the path to edit.
     */
    private String editPath(String path)
    {
        String currentDir = System.getProperty("user.dir");
        path = currentDir + path;
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
        String destinationPath = "manuals/" + module.getModuleCode() + ".pdf";
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

    /**
     * Throws an OutputIOException when there has been an IOException at some point in the file writing process.
     * Avoids repetition of adding identical causes when exceptions are thrown for very similar problems.
     * @throws OutputIOException - Exception with details about possible causes and resolutions.
     */
    private void throwOutIO() throws OutputIOException
    {
        OutputIOException oioe = new OutputIOException();
        oioe.addPossibleCause("Cannot write a file to this program's location.");
        oioe.addPossibleResolution("Retry with a different manual file name.");
        oioe.addPossibleResolution("Reinstall the application in a different location.");
        throw oioe;
    }
}
