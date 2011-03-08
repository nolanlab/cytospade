import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.cytopanels.CytoPanelImp;
import cytoscape.*;
import cytoscape.actions.LoadNetworkTask;
import cytoscape.data.CyAttributes;
import cytoscape.data.SelectEventListener;
import cytoscape.logger.CyLogger;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyDependency;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.BoundaryRangeValues;
import cytoscape.visual.mappings.ContinuousMapping;
import cytoscape.visual.mappings.Interpolator;
import cytoscape.visual.mappings.LinearNumberToColorInterpolator;
import cytoscape.visual.mappings.LinearNumberToNumberInterpolator;
import cytoscape.visual.mappings.ObjectMapping;

import facs.CanvasSettings;
import facs.Plot2D;
import giny.model.GraphPerspective;
import giny.view.NodeView;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;

/**
 * Cytoscape plugin that draws scatter plots for SPADE trees
 */
public class CytoSpade extends CytoscapePlugin {

    private SPADEContext spadeCxt;
    private VisualMapping visualMapping;

    /**
     * Creates an action and adds it to the Plugins menu.
     */
    public CytoSpade() {
        // Initialized internal state keeping
        spadeCxt = new SPADEContext();
        
        //create a new action to respond to menu activation
        SPADEdraw action = new SPADEdraw();
        //set the preferred menu
        action.setPreferredMenu("Plugins");
        //and add it to the menus
        Cytoscape.getDesktop().getCyMenus().addAction(action);
    }

    

    /**
     * Sets up the CanvasSettings and drawPlot method used to generated the
     * FACS plots.
     * @throws IOException
     */
    private void drawScatter() throws IOException {
        
    }

    

    /**
     * Closes the SOUTH CytoSPADE pane
     * @param evt
     */
    private void closeButtonSouthClicked(java.awt.event.ActionEvent evt) {
        int returnvalue = JOptionPane.showConfirmDialog(null, "Close SPADE plug-in?", "Confirm close", JOptionPane.OK_CANCEL_OPTION);
        if(returnvalue == JOptionPane.OK_OPTION) {
            //FIXME This will fail if the user loads another plug-in after loading SPADE
            Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH).remove(Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH).getCytoPanelComponentCount()-1);
            return;
        } else {
            return;
        }
     }

    /**
     * Platform-dependent finder of Rscript
     * @return - the full path to Rscript
     */
    private String findR() {
        String RPath = null;
        if( getOS().matches("windows")) {
            //We can query the registry
            //Don't try R64, because SPADE isn't yet compatible with it.
            ////Try R64 first
            //RPath = queryRegistry("HKLM\\SOFTWARE\\R-core\\R64","InstallPath");
            if (RPath == null) {
                //Try R[32] second
                RPath = queryRegistry("HKLM\\SOFTWARE\\R-core\\R","InstallPath");
                if (RPath == null) {
                    //Give up
                    JOptionPane.showMessageDialog(null, "Unable to locate R. Please select the location of Rscript.exe.");
                    JFileChooser RfileChooser = new JFileChooser();
                    int returnValue = RfileChooser.showOpenDialog(RfileChooser);
                    if(returnValue == JFileChooser.APPROVE_OPTION) {
                        RPath = RfileChooser.getSelectedFile().getPath();
                        if (!RPath.endsWith("Rscript.exe")) {
                            JOptionPane.showMessageDialog(null, "Invalid selection. Please make sure you selected Rscript.exe, not R.exe");
                            return null;
                        } else {
                            return RPath;
                        }
                    } else if (returnValue == JFileChooser.CANCEL_OPTION) {
                        return null;
                    } else {
                        return null;
                    }
                } else {
                    //We found R[32]
                    return RPath + "\\bin\\Rscript.exe";
                }
            } else {
                //Won't evaluate without trying R64 first
                //We found R64
                return RPath + "\\bin\\x64\\Rscript.exe";
            }

        } else {

            RPath = posixWhich("Rscript");
            
            //Give up
            if (RPath == null) {
                JOptionPane.showMessageDialog(null, "Unable to locate R. Please select the location of Rscript.");
                JFileChooser RfileChooser = new JFileChooser();
                int returnValue = RfileChooser.showOpenDialog(RfileChooser);
                if(returnValue == JFileChooser.APPROVE_OPTION) {
                    RPath = RfileChooser.getSelectedFile().getPath();
                    if (!RPath.endsWith("Rscript")) {
                        JOptionPane.showMessageDialog(null, "Invalid selection. Please make sure you selected Rscript, not R");
                        return null;
                    } else {
                        return RPath;
                    }
                } else if (returnValue == JFileChooser.CANCEL_OPTION) {
                    return null;
                } else {
                    return null;
                }
            } else {
                return RPath;
            }


        }

    }

    /**
     * Runs runSPADE.R
     * @param evt
     * @param plotsOnly - whether to run runSPADE.R or printPDFs.R
     */
    private void runSPADE(java.awt.event.ActionEvent evt, boolean plotsOnly) {
        JOptionPane.showMessageDialog(null, "All status and error messages from SPADE will be written to log.runSPADE.(date).txt.");

        SimpleDateFormat erinDateFormat = new SimpleDateFormat("yyMMdd_kk-mm-ss");
        String erinsDate = erinDateFormat.format(new Date());

        String RPath = findR();

        //This is an absolutely retarded way to run R files, but is the only way
        //I've managed to do it given that Java internalizes stdout/stderr, that
        //the path/to/R can contain spaces, and the way R behaves.

        try {

            FileWriter temp;
            if( getOS().matches("windows")) {
                //Write a .bat file
                temp = new FileWriter(new File(spadeCxt.getPath(), "runspade.bat").getAbsolutePath());
            } else {
                //Posix breed, write a .sh
                temp = new FileWriter(new File(spadeCxt.getPath(), "runspade.sh").getAbsolutePath());
            }

            BufferedWriter out = new BufferedWriter(temp);

            if( !getOS().matches("windows")) {
                //Posix breed
                out.write("cd \""+ spadeCxt.getPath().getAbsolutePath() + "\"" + "\n");
            }

            if( getOS().matches("windows")) {
                out.write("cmd /c \"");
            } else {
                //Posix breed
                out.write("/bin/sh -c \"");
            }

            //Note: Windows will almost always have spaces in the path to R, e.g.
            //C:\Program Files\R, so it for sure needs the quotes around the path.
            //Not sure if posix needs quotes, since the path/to/R probably won't
            //have spaces in it.
            if( getOS().matches("windows")) {
                out.write("\"");
                out.write(RPath);
                out.write("\"");
            } else {
                out.write(RPath);
            }
            
            out.write(" --vanilla ");

            if(!plotsOnly) {
                if (getOS().matches("windows")) {
                    out.write("\"" + new File(spadeCxt.getPath(), "runSPADE.R").getAbsolutePath()+ "\"");
                } else {
                    out.write("runSPADE.R");
                }
            } else {
                out.write("printPDFs.R");
            }
            out.write(" -num_threads=");
            out.write(String.valueOf(Runtime.getRuntime().availableProcessors()));
            out.write("\" >\"");
            out.write(new File(spadeCxt.getPath(),"log.runSPADE."+erinsDate+".txt").getAbsolutePath());
            out.write("\" 2>&1");

            if(plotsOnly) {
                //Delete printPDFs file
                if( getOS().matches("windows")) {
                    out.write("\r\n");
                    out.write("rm \"" + new File(spadeCxt.getPath(), "printPDFs.R").getAbsolutePath()+"\n");
                } else {
                    //Posix breed
                    out.write("\n");
                    out.write("rm \"" + new File(spadeCxt.getPath(), "printPDFs.R").getAbsolutePath()+"\"");
                }
            }

            //Make batch/shell file delete itself
            if( getOS().matches("windows")) {
                out.write("\r\n");
                out.write("rm \"" + new File(spadeCxt.getPath(), "runspade.bat").getAbsolutePath()+"\n");
            } else {
                //Posix breed
                out.write("\n");
                out.write("rm \"" + new File(spadeCxt.getPath(), "runspade.sh").getAbsolutePath()+"\"");
            }

            out.close();

            //Exec the file
            if( getOS().matches("windows")) {
                Process pp = Runtime.getRuntime().exec(new File(spadeCxt.getPath(), "runspade.bat").getAbsolutePath());
            } else {
                //Posix breed
                //Make executable first
                String[] chmod = {"chmod","+x", new File(spadeCxt.getPath(), "runspade.sh").getAbsolutePath()};
                Process ppp = Runtime.getRuntime().exec(chmod);
                Process pp = Runtime.getRuntime().exec(new String[] {new File(spadeCxt.getPath(), "runspade.sh").getAbsolutePath(), ""});
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e);
            return;
        }


    }

    /**
     * Wrapper for WHICH in posix environment
     * @param app - path to key
     * @return - returns value if it exists, null if not exists
     */
    private String posixWhich(String app) {
        String line;
        try {
            Process p = Runtime.getRuntime().exec("which " + app);
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            p.waitFor();
            line=reader.readLine(); //contains the value
            if (p.exitValue() != 0) {
                //semi-sloppy way of knowing the key doesn't exist
                return null;
            }
            return line;
        } catch (Exception e) {
            return null;
        }

    }


    /**
     * Queries the Windows registry
     * @param path - path to key
     * @param key - key to query
     * @return - returns value if it exists, null if not exists
     */
    private String queryRegistry(String path, String key) {
        String line;
        try {
            Process p = Runtime.getRuntime().exec("REG QUERY " + '"'+ path + "\" /v " + key);
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            p.waitFor();
            reader.readLine(); //null
            reader.readLine(); //echo
            line=reader.readLine(); //contains the value
            if (p.exitValue() != 0) {
                //semi-sloppy way of knowing the key doesn't exist
                return null;
            }
            String[] parsed = line.split("    ");
            return parsed[parsed.length-1];
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(null, e);
            return null;
        }

    }


    


    /**
     * Event to save the user-defined network landscaping when Cytoscape exits.
     */
    @Override
    public void onCytoscapeExit() {
        saveLandscaping(true);
    }

    /**
     * Saves the user-defined network landscaping to a flat file
     * @param closeNetwork - whether or not to close the network after saving it.
     */
    private void saveLandscaping(Boolean closeNetwork) {
        CyNetworkView cnv = Cytoscape.getCurrentNetworkView();
        GraphPerspective network = (GraphPerspective) Cytoscape.getCurrentNetwork();
        Iterator<CyNode> nodesIt;
        if(!network.nodesList().isEmpty()) {
            try {
                FileWriter fstream = new FileWriter(new File(spadeCxt.getPath(), "layout.table").getAbsolutePath());
                BufferedWriter out = new BufferedWriter(fstream);

                nodesIt = network.nodesIterator();

                //This stupid iterator runs backward. So reverse the list first
                double[] xPositions = new double[network.nodesList().size()];
                double[] yPositions = new double[network.nodesList().size()];
                int ii = network.nodesList().size() - 1;
                while (nodesIt.hasNext()) {
                    giny.model.Node cytoNode = (giny.model.Node) nodesIt.next();
                    NodeView nodeView = cnv.getNodeView(cytoNode);
                    if (nodeView == null) {
                        JOptionPane.showMessageDialog(null, "Error: null nodeView");
                    }
                    xPositions[ii]=nodeView.getXPosition();
                    //Multiply by -1 to flip map
                    yPositions[ii]= -1*nodeView.getYPosition();
                    ii--;
                }

                //Now write the list out
                for (int i = 0; i < network.nodesList().size(); i++) {
                        out.write(xPositions[i]+" ");
                        out.write(yPositions[i]+"\n");
                }

                out.close();
            } catch (IOException ex) {
                Logger.getLogger(CytoSpade.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        }

        if (closeNetwork) {
            //Close the network that the user just left
            Cytoscape.destroyNetwork(Cytoscape.getCurrentNetwork());
            //This is the only way to clear the nodeAttributes. I don't really
            //know what it does though; found it by trial-and-error:
            Cytoscape.createNewSession();
            //(It's necessary to clear the nodeAttributes for the sake of 
            //mapColor's functionality.
        }
    }

    /**
     * Reads and applies the user-defined network landscaping from a flat file
     * @param layoutFile
     */
    private void readLandscaping(File layoutFile) {
        CyNetworkView cnv = Cytoscape.getCurrentNetworkView();
        GraphPerspective network = (GraphPerspective) Cytoscape.getCurrentNetwork();
        Iterator<CyNode> nodesIt;
        try {
            FileReader fstream = new FileReader(layoutFile.getAbsolutePath());
            BufferedReader in = new BufferedReader(fstream);
            String[] line = new String[2];

            cnv = Cytoscape.getCurrentNetworkView();
            network = (GraphPerspective) Cytoscape.getCurrentNetwork();
            nodesIt = network.nodesIterator();

            //This stupid iterator runs backward. So reverse the list first
            double[] xPositions = new double[network.nodesList().size()];
            double[] yPositions = new double[network.nodesList().size()];
            for (int i = network.nodesList().size()-1; i > -1; i--) {
                try {
                    line = in.readLine().split(" ");
                } catch (IOException ex) {
                    Logger.getLogger(CytoSpade.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null, "IO error while reading in relandscaped network");
                }
                xPositions[i] = Double.parseDouble(line[0]);
                yPositions[i] = Double.parseDouble(line[1]);
            }

            //Now apply it.
            int ii = 0;
            while (nodesIt.hasNext()) {
                giny.model.Node cytoNode = (giny.model.Node) nodesIt.next();
                NodeView nodeView = cnv.getNodeView(cytoNode);
                if (nodeView != null) {
                    nodeView.setXPosition(xPositions[ii]);
                    //Multiply by -1 to flip the map
                    nodeView.setYPosition(-1*yPositions[ii]);
                    ii++;
                }

            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(CytoSpade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    

   
    /**
     * Writes out the runSPADE.R file
     * @param evt - not used
     * @param plotsOnly - true to only draw plots, false to run SPADE entirely
     * @param quiet - silence messages
     */
    private void authorRS(java.awt.event.ActionEvent evt, boolean plotsOnly, boolean quiet) {
/*
        try {
            FileWriter fstream;
            if(!plotsOnly) {
                fstream = new FileWriter(new File(FCSFilePath, "runSPADE.R").getAbsolutePath());
            } else {
                fstream = new FileWriter(new File(FCSFilePath, "printPDFs.R").getAbsolutePath());
            }
            BufferedWriter out = new BufferedWriter(fstream);

            out.write("#!/share/apps/R/R-2.11.1/bin/Rscript"+"\n");
            out.write("# ^^set this to your Rscript path"+"\n");
            out.write("#"+"\n");
            if(plotsOnly) {
                out.write("# THIS runSPADE SCRIPT PRODUCES PDFS ONLY"+"\n");
            }
            out.write("# runSPADE:  R wrapper script for SPADE tree construction"+"\n");
            out.write("# Erin Simonds - esimonds@stanford.edu"+"\n");
            out.write("# Version 2.4 - November 10, 2010"+"\n");
            out.write("# Autogenerated by CytoSPADE"+"\n");
            out.write("#"+"\n");
            out.write("# Command line instructions:"+"\n");
            out.write("#   1) Make sure the first line of this file is your Rscript path (found in the same directory as R)"+"\n");
            out.write("#"+"\n");
            out.write("#   2) Move the entire directory containing this script and the FCS file(s) to be analyzed to the computer on which you will run SPADE"+"\n");
            out.write("#"+"\n");
            out.write("#   3) In a command shell, navigate to the folder containing this script and the FCS file(s) to be analyzed"+"\n");
            out.write("#"+"\n");
            out.write("#   4) Make this script executable.  At the command line, run:"+"\n");
            out.write("#	$ chmod +x runSPADE.R"+"\n");
            out.write("#"+"\n");
            out.write("#   5a) For normal use (not in a load sharing or compute cluster): At the command line, run:"+"\n");
            out.write("#	$ ./runSPADE.R [-num_threads=X] [-file_to_process=Y]"+"\n");
            out.write("#	where X is the number of threads you wish to use (default = 1)"+"\n");
            out.write("#	Y is the name of the file to process (default = use file(s) specified in this script)"+"\n");
            out.write("#		Note that parameters in brackets may be omitted."+"\n");
            out.write("#"+"\n");
            out.write("#   5b) For Sun Gridengine: At the command line, run:"+"\n");
            out.write("#	$ qsub -cwd -j y -b y -m e -M username@domain.ext [-pe threaded A] ./runSPADE.R [-num_threads=X] [-file_to_process=Y]"+"\n");
            out.write("#	where username@domain.ext is your e-mail address to e-mail when the job is done,"+"\n");
            out.write("#	A is the number of slots to reserve with Gridengine,"+"\n");
            out.write("#	X is the number of threads to use in SPADE (usually the same as A) (default = 1)."+"\n");
            out.write("#	Y is the name of the file to process (default = use file(s) specified in this script)"+"\n");
            out.write("#		Note that parameters in brackets may be omitted."+"\n");
            out.write("#"+"\n");
            out.write("#   5c) For Platform LSF: At the command line, run:"+"\n");
            out.write("#	$ bsub [-n A -R \"span[hosts=1]\"] ./runSPADE.R [-num_threads=X] [-file_to_process=Y]"+"\n");
            out.write("#	A is the number of slots to reserve with LSF,"+"\n");
            out.write("#	X is the number of threads to use in SPADE (usually the same as A) (default = 1)."+"\n");
            out.write("#	Y is the name of the file to process (default = use file(s) specified in this script)"+"\n");
            out.write("#		Note that parameters in brackets may be omitted. (Note that the brackets surrounding \"hosts=1\" do not indicate an optional parameter.)"+"\n");
            out.write("#"+"\n");
            out.write("# Interactive instructions:"+"\n");
            out.write("#   1) Move the entire directory containing this script and the FCS file(s) to be analyzed to the computer on which you will run SPADE"+"\n");
            out.write("#   2) In an interactive R session, change the working directory to the folder created in step 1"+"\n");
            out.write("#   3) At the R command line, run: source(\"runSPADE.R\")"+"\n");
            out.write("#"+"\n");

            out.write("#BEGIN AUTOGENERATED DATA"+"\n");
            
            if(!plotsOnly) {

                out.write("FILE_TO_PROCESS=\".\""+"\n");

                out.write("SURFACE_MARKERS=c(");
                for (int i = 0; i < clusterPList.getSelectedIndices().length; i++) {
                    out.write("\""+ channelsPN[clusterPList.getSelectedIndices()[i]] +"\"");
                    if (i + 1 < clusterPList.getSelectedIndices().length) {
                        out.write(",");
                    }
                }
                out.write(")"+"\n");


                out.write("PANELS=list(\n");
                out.write("list(\n");

                out.write("panel_files=c(");
                for (int i = 0; i < filenames.length; i++) {
                    if (i > 0) {
                        out.write(",");
                    }
                    out.write("\"" + filenames[i] + "\"");
                }
                out.write("),\n");

                out.write("median_cols=NULL,\n");

                if (referenceFList.getSelectedValues().length > 0) {
                    out.write("reference_files=c(");
                    for (int i = 0; i < referenceFList.getSelectedValues().length; i++) {
                        out.write("\""+ referenceFList.getSelectedValues()[i] +"\"");
                        if (i + 1 < referenceFList.getSelectedValues().length) {
                            out.write(",");
                        }
                    }
                    out.write("),\n");
                } else {
                    out.write("reference_files=NULL,\n");
                }

                if (foldPList.getSelectedValues().length > 0) {
                    out.write("fold_cols=c(");
                    for (int i = 0; i < foldPList.getSelectedIndices().length; i++) {
                        out.write("\""+ channelsPN[foldPList.getSelectedIndices()[i]] +"\"");
                        if (i + 1 < foldPList.getSelectedIndices().length) {
                            out.write(",");
                        }
                    }
                    out.write(")\n");
                } else {
                    out.write("fold_cols=NULL\n");
                }

                out.write(")\n");
                out.write(");\n");



                out.write("ALL_MARKERS=NULL"+"\n");

                

                out.write("ARCSINH_COFACTOR=");
                out.write(arcsinhSpinner.getValue().toString());
                out.write("\n");

                out.write("DOWNSAMPLED_EVENTS=");
                out.write(downsampleTargetSpinner.getValue().toString());
                out.write("\n");

                out.write("CLUSTERING_SAMPLES=");
                out.write("50000");
                out.write("\n");

                out.write("DOWNSAMPLING_EXCLUDE_PCTILE=");
                out.write("0.01");
                out.write("\n");

                out.write("TARGET_CLUSTERS=");
                out.write(clusterTargetSpinner.getValue().toString());
                out.write("\n");

            }

            out.write("NORMALIZE=\"");
            if(!plotsOnly) {
                out.write(scaleNormCombo.getSelectedItem().toString());
            } else {
                JFrame frame = new JFrame();
                int local = JOptionPane.showOptionDialog(frame, "Select normalization method", "Normalization option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] {"global", "local"}, "local");
                if (local == 0) {
                    out.write("local");
                } else {
                    //note this is else, not else if...
                    out.write("global");
                }
            }
            out.write("\""+"\n");

            if(!plotsOnly) {
                out.write("OUTPUT_DIR=\"output/\""+"\n");
            } else {
                out.write("OUTPUT_DIR=\"./\""+"\n");
            }

            out.write("TMPDIR=\"/tmp/\""+"\n");

            out.write("#END AUTOGENERATED DATA"+"\n");

            out.write("LIBRARY_PATH=NULL"+"\n");

            out.write("NUM_THREADS <- 1"+"\n");
            out.write("for (e in commandArgs()) {"+"\n");
            out.write("	ta <- strsplit(e,\"=\",fixed=TRUE)"+"\n");
            out.write("	if( ta[[1]][1] == \"-num_threads\") {"+"\n");
            out.write("		NUM_THREADS <- ta[[1]][2]"+"\n");
            out.write("	}"+"\n");
            out.write("	if( ta[[1]][1] == \"-file_to_process\") {"+"\n");
            out.write("		FILE_TO_PROCESS <- ta[[1]][2]"+"\n");
            out.write("	}"+"\n");
            out.write("}"+"\n");
            out.write("Sys.setenv(\"OMP_NUM_THREADS\"=NUM_THREADS)"+"\n");
            out.write("library(\"spade\",lib.loc=LIBRARY_PATH)"+"\n");
            out.write("LAYOUT_FUNCTION=layout.kamada.kawai"+"\n");
            if(!plotsOnly) {
                out.write("SPADE.driver(FILE_TO_PROCESS, file_pattern=\"*.fcs\", out_dir=OUTPUT_DIR, cluster_cols=SURFACE_MARKERS, panels=PANELS, arcsinh_cofactor=ARCSINH_COFACTOR, layout=LAYOUT_FUNCTION, downsampling_samples=DOWNSAMPLED_EVENTS, downsampling_exclude_pctile=DOWNSAMPLING_EXCLUDE_PCTILE, k=TARGET_CLUSTERS, clustering_samples=CLUSTERING_SAMPLES)"+"\n");
            }
            out.write("LAYOUT_TABLE <- read.table(paste(OUTPUT_DIR,\"layout.table\",sep=\"\"))"+"\n");
            out.write("MST_GRAPH <- read.graph(paste(OUTPUT_DIR,\"mst.gml\",sep=\"\"),format=\"gml\")"+"\n");
            out.write("SPADE.plot.trees(MST_GRAPH,OUTPUT_DIR,file_pattern=\"*fcs*Rsave\",layout=as.matrix(LAYOUT_TABLE),out_dir=paste(OUTPUT_DIR,\"pdf\",sep=\"\"))"+"\n");
            out.write("Sys.unsetenv(\"OMP_NUM_THREADS\")"+"\n");
            
            out.close();

            if(!quiet) {
                JOptionPane.showMessageDialog(null, "runSPADE.R file successfully written");
            }

        } catch (IOException ex) {
            Logger.getLogger(CytoSpade.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
*/
    }

    /**
     * Gets the OS
     */
    public String getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) {
            return "windows";
        } else if (os.indexOf("mac") >= 0) {
            return "macintosh";
        } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            return "unix";
        } else {
            JOptionPane.showMessageDialog(null, "Invalid OS detection");
            return "error";
        }
    }


    /**
     * Creates the SPADE runSPADE.R authoring panel.
     */
    /*
    class authorPanel extends JPanel {

        public authorPanel () {

            javax.swing.JButton authorRSbutton;
            javax.swing.JScrollPane jScrollPane1;
            javax.swing.JScrollPane jScrollPane2;
            javax.swing.JScrollPane jScrollPane3;
            javax.swing.JLabel jLabel2, jLabel1, jLabel3, jLabel4, jLabel5, jLabel6, jLabel7;
            javax.swing.JButton closeButtonSouth;
            javax.swing.JButton runSPADEbutton;

            scaleNormCombo = new javax.swing.JComboBox(new String[] {"global", "local"});
            clusterTargetSpinner = new javax.swing.JSpinner();
            downsampleTargetSpinner = new javax.swing.JSpinner();
            arcsinhSpinner = new javax.swing.JSpinner();
            jScrollPane1 = new javax.swing.JScrollPane();
            clusterPList = new javax.swing.JList();
            jScrollPane2 = new javax.swing.JScrollPane();
            foldPList = new javax.swing.JList();
            jScrollPane3 = new javax.swing.JScrollPane();
            referenceFList = new javax.swing.JList();
            jLabel2 = new javax.swing.JLabel();
            jLabel1 = new javax.swing.JLabel();
            jLabel3 = new javax.swing.JLabel();
            jLabel4 = new javax.swing.JLabel();
            jLabel5 = new javax.swing.JLabel();
            jLabel6 = new javax.swing.JLabel();
            jLabel7 = new javax.swing.JLabel();
            authorRSbutton = new javax.swing.JButton();
            closeButtonSouth = new javax.swing.JButton();
            runSPADEbutton = new javax.swing.JButton();

            closeButtonSouth.setText("Close");
            runSPADEbutton.setText("Run SPADE locally");
            runSPADEbutton.setToolTipText("Runs SPADE on this computer. Requires R with the SPADE package installed.");

            clusterTargetSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(200), Integer.valueOf(0), null, Integer.valueOf(1)));
            downsampleTargetSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(5000), Integer.valueOf(0), null, Integer.valueOf(1)));

            clusterPList.setModel(new javax.swing.AbstractListModel() {
                String[] strings = channels;
                public int getSize() { return strings.length; }
                public Object getElementAt(int i) { return strings[i]; }
            });
            jScrollPane1.setViewportView(clusterPList);

            foldPList.setModel(new javax.swing.AbstractListModel() {
                String[] strings = channels;
                public int getSize() { return strings.length; }
                public Object getElementAt(int i) { return strings[i]; }
            });
            jScrollPane2.setViewportView(foldPList);

            referenceFList.setModel(new javax.swing.AbstractListModel() {
                //TODO make this files
                String[] strings = filenames;
                public int getSize() { return strings.length; }
                public Object getElementAt(int i) { return strings[i]; }
            });
            jScrollPane3.setViewportView(referenceFList);

            jLabel1.setText("1. Select tree clustering parameters");
            clusterPList.setToolTipText("1 or more parameter; use ctrl+click and shift+click to select multiples");
            jLabel2.setText("2. Select fold-change parameters");
            foldPList.setToolTipText("0 or more parameters; use ctrl+click and shift+click to select multiples");
            jLabel3.setText("3. Select reference file(s) for fold change");
            referenceFList.setToolTipText("1 or more files; use ctrl+click and shift+click to select multiples");
            jLabel4.setText("4. Enter arcsinh value (CyTOF=5, fluorescence=150)");
            jLabel5.setText("5. Select scale normalization");
            jLabel6.setText("6. Enter target number of clusters");
            clusterTargetSpinner.setToolTipText("Tree will contain +/- 50% clusters");
            jLabel7.setText("7. Enter target number of downsampled events");

            authorRSbutton.setText("8. Author runSPADE file");

            arcsinhSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(5), Integer.valueOf(0), null, Integer.valueOf(5)));

            authorRSbutton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    authorRS(evt, false, false);
                }
            });

            runSPADEbutton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    runSPADE(evt, false);
                }
            });

            closeButtonSouth.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    closeButtonSouthClicked(evt);
                }
            });

            int combowidth = 125;
            if (getOS().equals("windows")) {
                combowidth = 75;
            }

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(190, 190, 190))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel3)
                                .addComponent(arcsinhSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel5)
                                .addComponent(scaleNormCombo, javax.swing.GroupLayout.PREFERRED_SIZE, combowidth, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel6)
                                .addComponent(clusterTargetSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel7)
                                .addComponent(downsampleTargetSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(authorRSbutton)
                                .addComponent(runSPADEbutton)
                                .addComponent(closeButtonSouth))
                            .addContainerGap(380, Short.MAX_VALUE))))
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jLabel2)
                        .addComponent(jLabel3))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel4)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(arcsinhSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(scaleNormCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel6)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(clusterTargetSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(downsampleTargetSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(authorRSbutton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(runSPADEbutton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(closeButtonSouth)))
                    .addContainerGap())
            );

        }
    }
*/
    /**
     * Creates the SPADE controls panel.
     */
    class SpadePanel extends JPanel {

        public SpadePanel(SPADEContext spadeCxt){
            this.spadeCxt = spadeCxt;

             // Find the global_boundaries.table file it exists, and create appropiate visual mapping
            File[] boundaryFiles = spadeCxt.getPath().listFiles(new FilenameFilter() {
                public boolean accept(File f, String name) {
                    return (name.matches("global_boundaries.table"));
                }
            });
            if (boundaryFiles.length == 1)
                this.visualMapping = new VisualMapping(boundaryFiles[0]);
            else if (boundaryFiles.length == 0)
                this.visualMapping = new VisualMapping();
            else {
                JOptionPane.showMessageDialog(null, "Error: Found more than one global_boundaries.table file.");
                return;
            }

            initComponents(); 
        }

        private void initComponents() {
            // <editor-fold defaultstate="collapsed" desc="GUI Code">
            javax.swing.JButton closeButtonWest = new javax.swing.JButton();
            closeButtonWest.setText("Close");

            javax.swing.JButton drawPlotsButton = new javax.swing.JButton();
            drawPlotsButton.setText("Produce PDFs");
            drawPlotsButton.setEnabled(false);

            javax.swing.JLabel FilenameLbl = new javax.swing.JLabel("File");
            filenameComboBox = new javax.swing.JComboBox(spadeCxt.getFCSFiles());
            filenameComboBox.setMaximumRowCount(20);
            filenameComboBox.setRenderer(new javax.swing.ListCellRenderer() {
                // Render FCS files as just File name (no path information, or long extensions)
                public Component getListCellRendererComponent(JList jlist, Object o, int idx, boolean isSelected, boolean bln1) {
                    String name = "";
                    if (o != null) {
                        name = ((File)o).getName();
                        name = name.substring(0, name.lastIndexOf(".density.fcs.cluster.fcs"));
                    }
                    JLabel label = new JLabel(name);

                    label.setBackground(isSelected ? jlist.getSelectionBackground(): jlist.getBackground());
                    label.setForeground(isSelected ? jlist.getSelectionForeground(): jlist.getForeground());
                    label.setEnabled(jlist.isEnabled());
                    label.setFont(jlist.getFont());
                    label.setOpaque(true);

                    return label;
                }
            });
            filenameComboBox.setSelectedIndex(-1);
            filenameComboBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    filenameComboBoxActionPerformed(evt);
                }
            });


            javax.swing.JLabel colorscaleLabel = new javax.swing.JLabel("Coloring attribute");
            colorscaleComboBox = new javax.swing.JComboBox();
            colorscaleComboBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    colorscaleComboBoxActionPerformed(evt);
                }
            });

            javax.swing.JLabel colorrangeLabel = new javax.swing.JLabel("Coloring range");
            colorrangeComboBox = new javax.swing.JComboBox();
            colorrangeComboBox.setModel(new javax.swing.DefaultComboBoxModel(
                    visualMapping.globalRangeAvailable() ?
                        new VisualMapping.RangeKind[] { VisualMapping.RangeKind.GLOBAL, VisualMapping.RangeKind.LOCAL } :
                        new VisualMapping.RangeKind[] { VisualMapping.RangeKind.LOCAL }
            ));
            colorrangeComboBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    colorrangeComboBoxActionPerformed(evt);
                }
            });

            javax.swing.JLabel howtoadjust = new javax.swing.JLabel("Click axis label to change parameter and scale");

            javax.swing.JLabel StyleLbl = new javax.swing.JLabel("Style");
            plotStyleComboBox = new javax.swing.JComboBox();
            plotStyleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Shaded Contour", "Dot", "Density Dot", "Shadow", "Contour", "Density" }));


            jLabelPlot = new javax.swing.JLabel();
            jLabelPlot.setBounds(0,0,358,358);

            countLabel = new javax.swing.JLabel();
            xAxisClickable = new javax.swing.JLabel();
            xAxisClickable.setBounds(48,311,308,46);

            yAxisClickable = new javax.swing.JLabel();
            yAxisClickable.setBounds(0,0,46,308);

            plotArea = new javax.swing.JLayeredPane();
            

            plotStyleComboBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    plotStyleComboActionPerformed(evt);
                }
            });

            closeButtonWest.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    closeButtonWestClicked(evt);
                }
            });

            drawPlotsButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    saveLandscaping(false);
                    authorRS(evt, true, true);
                    runSPADE(evt, true);
                }
            });

            xAxisPopup = new javax.swing.JPopupMenu();
            yAxisPopup = new javax.swing.JPopupMenu();
            
            xAxisClickable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent me) {
                    xAxisPopup.show(xAxisClickable,154,23);
                }
            });

            yAxisClickable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent me) {
                    yAxisPopup.show(yAxisClickable,23,154);
                }
            });

            plotArea.add(xAxisClickable);
            plotArea.add(yAxisClickable);
            plotArea.add(jLabelPlot);

            //Platform-dependent width of small comboboxes
            int combowidth = 125;
            if (getOS().equals("windows")) {
                combowidth = 75;
            }

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(howtoadjust)
                        .addComponent(plotArea, 358, 358, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(StyleLbl)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(plotStyleComboBox, 0, 165, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(FilenameLbl)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(filenameComboBox, 0, 165, Short.MAX_VALUE))))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(colorscaleLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(colorscaleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(colorrangeLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(colorrangeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(countLabel)
                        .addComponent(drawPlotsButton)
                        .addComponent(closeButtonWest))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(FilenameLbl)
                        .addComponent(filenameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(colorscaleLabel)
                        .addComponent(colorscaleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(colorrangeLabel)
                        .addComponent(colorrangeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(howtoadjust)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(StyleLbl)
                        .addComponent(plotStyleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(plotArea, 358, 358, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(countLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(drawPlotsButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(closeButtonWest)
                    .addContainerGap(19, Short.MAX_VALUE))
            );
            // </editor-fold>
        }

        /**
         * Handles node selection events
         */
        public class HandleSelect implements SelectEventListener {

            public HandleSelect() { }

            public void onSelectEvent(cytoscape.data.SelectEvent e) {
                (new drawScatterThread()).execute();
            }
        }

         public class XactionPerformed implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().matches("Linear")         ||
                    e.getActionCommand().matches("Log")            ||
                    e.getActionCommand().matches("Arcsinh: CyTOF") ||
                    e.getActionCommand().matches("Arcsinh: Fluor") ) {
                    xChanScale = e.getActionCommand();
                } else {
                    xChanParam = e.getActionCommand();
                }
                (new drawScatterThread()).execute();
            }
        }

        public class YactionPerformed implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().matches("Linear")         ||
                    e.getActionCommand().matches("Log")            ||
                    e.getActionCommand().matches("Arcsinh: CyTOF") ||
                    e.getActionCommand().matches("Arcsinh: Fluor") ) {
                    yChanScale = e.getActionCommand();
                } else {
                    yChanParam = e.getActionCommand();
                }
                (new drawScatterThread()).execute();
            }
        }

        /**
         * When the filenameComboBox is changed, changes the display if the selection
         * isn't null (item 0), applies the landscaping, adds a SelectEvent listener,
         * zooms to the network, draws a plot, adds color scaling attributes to the
         * combo box, and maps node sizes. Eventually will map colors too.
         * @param evt
         */
        private void filenameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
            
            CyNetworkView cnv = Cytoscape.getCurrentNetworkView();
            GraphPerspective network = (GraphPerspective) Cytoscape.getCurrentNetwork();

            //Close the current network, saving the X and Y coords for reuse
            //This is a hackerish way to tell if no network is loaded. For some reason,
            //Cytoscape.getCurrentNetwork[View]() always returns something.
            if (!network.nodesList().isEmpty()) {
                saveLandscaping(true);
            }

            //Open the new network, applying the X and Y coords if available
            if (filenameComboBox.getSelectedIndex() >= 0) {
                LoadNetworkTask.loadFile(spadeCxt.getGMLFiles()[filenameComboBox.getSelectedIndex()], true);

                //Find the layout.table file if it exists
                File[] layoutFiles = spadeCxt.getPath().listFiles(new FilenameFilter() {
                    public boolean accept(File f, String name) {
                        return (name.matches("layout.table"));
                    }
                });
                if (layoutFiles.length == 1) {
                    readLandscaping(layoutFiles[0]);
                } else if(layoutFiles.length > 1) {
                    JOptionPane.showMessageDialog(null, "Error: Found more than one layout.table file");
                    return;
                }

                // Add listener for updating dot plot based on user node selection
                Cytoscape.getCurrentNetwork().addSelectEventListener(new HandleSelect());

                //Zoom to the network
                Cytoscape.getCurrentNetworkView().fitContent();

                // Update the parameter combo box
                VisualMapping.populateNumericAttributeComboBox(colorscaleComboBox);
                colorscaleComboBox.setSelectedIndex(0);

                // Update plot combo boxes with channels in new FCS files
                xAxisPopup = new javax.swing.JPopupMenu();
                yAxisPopup = new javax.swing.JPopupMenu();
                for (String scales: new String[] {"Linear","Log","Arcsinh: CyTOF","Arcsinh: Fluor"} ) {
                    menuItem = new JMenuItem(scales);
                    menuItem.addActionListener(new SpadePanel.XactionPerformed());
                    xAxisPopup.add(menuItem);
                    menuItem = new JMenuItem(scales);
                    menuItem.addActionListener(new SpadePanel.YactionPerformed());
                    yAxisPopup.add(menuItem);
                }

                xAxisPopup.addSeparator();
                yAxisPopup.addSeparator();
                
                fcsFile FCSInputFile = null;
                try {
                    FCSInputFile = new fcsFile((File)filenameComboBox.getSelectedItem(), true);
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "File not found.");
                    return;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error reading file.");
                    return;
                }

                for (int i=0; i<FCSInputFile.getChannelCount(); i++) {
                    menuItem = new JMenuItem(FCSInputFile.getChannelShortName(i));
                    menuItem.addActionListener(new SpadePanel.XactionPerformed());
                    xAxisPopup.add(menuItem);
                    menuItem = new JMenuItem(FCSInputFile.getChannelShortName(i));
                    menuItem.addActionListener(new SpadePanel.YactionPerformed());
                    yAxisPopup.add(menuItem);
                }
                
                // Initialize plot axes parameters
                xChanScale = "Log";
                yChanScale = "Log";
                if (FCSInputFile.getChannelCount() > 0) {
                    xChanParam = FCSInputFile.getChannelShortName(0);
                    yChanParam = FCSInputFile.getChannelShortName(0);
                }

                //Draw a plot
                (new drawScatterThread()).execute();

                mapSizeAndColors();

            } else {
                //If the user selected the empty first row, clear the display
                countLabel.setText(null);
                jLabelPlot.setIcon(null);
            }

        }

        /*
         * Wraps the plotting function in a worker thread
         */
        public class drawScatterThread extends SwingWorker<Integer, Void> {

            private int COUNT;
            private double[] datax;
            private double[] datay;
            private double[] dataAx;
            private double[] dataAy;
            private double xChanMax;
            private double yChanMax;

            @Override
            protected Integer doInBackground() {
                try {
                    //These need to be null if there are no events selected, otherwise all
                    //events will be in the background and foreground.
                    //Fear not, populateData() makes them not-null if nodes are selected.
                    dataAx = null;
                    dataAy = null;

                    populateData();

                    int xDisplay, yDisplay;

                    if (xChanScale.matches("Linear")) {
                        xDisplay = Plot2D.LINEAR_DISPLAY;
                    } else if (xChanScale.matches("Log")) {
                        xDisplay = Plot2D.LOG_DISPLAY;
                    } else if (xChanScale.matches("Arcsinh: CyTOF")) {
                        xDisplay = Plot2D.ARCSINH_DISPLAY_CYTOF;
                    } else {
                        xDisplay = Plot2D.ARCSINH_DISPLAY_FLUOR;
                    }

                    if (yChanScale.matches("Linear")) {
                        yDisplay = Plot2D.LINEAR_DISPLAY;
                    } else if (yChanScale.matches("Log")) {
                        yDisplay = Plot2D.LOG_DISPLAY;
                    } else if (yChanScale.matches("Arcsinh: CyTOF")) {
                        yDisplay = Plot2D.ARCSINH_DISPLAY_CYTOF;
                    } else {
                        yDisplay = Plot2D.ARCSINH_DISPLAY_FLUOR;
                    }

                    //{ "Shaded Contour", "Dot", "Density Dot", "Shadow", "Contour", "Density" }
                    int plottype = 0;
                    if (plotStyleComboBox.getSelectedIndex() == 0) {
                        plottype = facs.Illustration.SHADED_CONTOUR_PLOT;
                    } else if (plotStyleComboBox.getSelectedIndex() == 1) {
                        plottype = facs.Illustration.DOT_PLOT;
                    } else if (plotStyleComboBox.getSelectedIndex() == 2) {
                        plottype = facs.Illustration.DENSITY_DOT_PLOT;
                    } else if (plotStyleComboBox.getSelectedIndex() == 3) {
                        plottype = facs.Illustration.SHADOW_PLOT;
                    } else if (plotStyleComboBox.getSelectedIndex() == 4) {
                        plottype = facs.Illustration.CONTOUR_PLOT;
                    } else if (plotStyleComboBox.getSelectedIndex() == 5) {
                        plottype = facs.Illustration.DENSITY_PLOT;
                    }

                  // Note that the size is set by axisBins

                    int dotSize;
                    if (COUNT > 5000) {
                        dotSize = 1;
                    } else if (COUNT > 1000) {
                        dotSize = 2;
                    } else {
                        dotSize = 3;
                    }

                    CanvasSettings cs = CanvasSettings.getCanvasSettings(10, 10, 0, 1, 2, plottype, facs.Illustration.DEFAULT_COLOR_SET, false, true, true, true, true, false, 300, 1.0d, 1.0d, 10.0d, 10.0d, facs.Illustration.DEFAULT_POPULATION_TYPE, COUNT, dotSize);
                    BufferedImage image = facs.Plot2D.drawPlot(cs, datax, datay, dataAx, dataAy, (String)xChanParam, (String)yChanParam, xChanMax, yChanMax, xDisplay, yDisplay);
                    jLabelPlot.setIcon(new ImageIcon(image));
                    return 0;
                } catch (IOException ex) {
                    Logger.getLogger(CytoSpade.class.getName()).log(Level.SEVERE, null, ex);
                    return 1;
                }
            }

            /**
             * Populates the data[A]{X,Y} arrays based on the selected file and the
             * selected node(s). (Uses only global variables.)
             */
            private void populateData() {

                //Get the selected nodes; return 0 if no nodes selected
                int[] selectedClust = null;
                CyNetwork current_network = Cytoscape.getCurrentNetwork();
                if (current_network != null) {
                    Set selectedNodes = current_network.getSelectedNodes();
                    if ( selectedNodes.isEmpty() ) {
                        //selectedClust = null; //Do nothing, selectedClust is initialized.
                    } else if ( selectedNodes.size() > 0 ) {
                        Object[] nds = (Object[])selectedNodes.toArray(new Object[1]);
                        selectedClust = new int[nds.length];
                        for (int i = 0; i < nds.length; i++) {
                            selectedClust[i] = Integer.parseInt(nds[i].toString())+1; //Plus 1!
                        }
                    } else {
                        return;
                    }
                }

                //Open the FCS file
                fcsFile FCSInputFile = null;
                try {
                    FCSInputFile = new fcsFile((File)filenameComboBox.getSelectedItem(), true);
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "File not found.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error reading file.");
                }

                //Pull the events list
                double[][] events = FCSInputFile.getEventList();

                //Find the columns with the appropriate parameters
                int xChan = 0;
                for (int i = 0; i < FCSInputFile.getChannelCount(); i++) {
                    if (FCSInputFile.getChannelShortName(i).contentEquals(xChanParam)){
                        xChan = i;
                    }
                }
                int yChan = 0;
                for (int i = 0; i < FCSInputFile.getChannelCount(); i++) {
                    if (FCSInputFile.getChannelShortName(i).contentEquals(yChanParam)){
                        yChan = i;
                    }
                }

                int num_events = FCSInputFile.getEventCount();

                //The cluster channel is always the last
                int clustChan = FCSInputFile.getChannelCount() - 1;

                DecimalFormat df = new DecimalFormat();

                if (selectedClust == null) {
                    datax = events[xChan];
                    datay = events[yChan];

                    countLabel.setText("Displaying " + df.format(num_events) + " of " + df.format(num_events) + " events");
                    COUNT = num_events;
                } else {

                    //The background events (all events)
                    dataAx = events[xChan];
                    dataAy = events[yChan];

                    //The primary events (selected only)
                    int eventcount = 0;
                    datax  = new double[num_events];
                    datay  = new double[num_events];

                    for( int clust = 0; clust < selectedClust.length; clust ++) {
                        for (int i = 0; i < num_events; i++) {
                            if (events[clustChan][i] == selectedClust[clust]) {
                                datax[i] = events[xChan][i];
                                datay[i] = events[yChan][i];
                                eventcount++;
                            }
                        }
                    }
                    countLabel.setText("Displaying " + df.format((int)eventcount) + " of " + df.format(num_events) + " events");
                    COUNT = eventcount;
                }

                xChanMax = FCSInputFile.getChannelRange(xChan);
                yChanMax = FCSInputFile.getChannelRange(yChan);

            }

        }

        /**
         * Applies sizes and colors to the network view
         */
        private void mapSizeAndColors() {
            // Skip mapping if no file is specified
            if((filenameComboBox.getSelectedIndex() < 0) || (colorscaleComboBox.getSelectedIndex() < 0)) {
                return;
            }

            try {
                visualMapping.setCurrentMarkersAndRangeKind(
                        "percenttotal",
                        colorscaleComboBox.getSelectedItem().toString(),
                        (VisualMapping.RangeKind)colorrangeComboBox.getSelectedItem()
                );
            } catch(IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, "Invalid choice of mapping parameters: "+e);
                return;
            }

            VisualMappingManager cyVMM = Cytoscape.getVisualMappingManager();

            try {

                VisualStyle spadeVS = cyVMM.getCalculatorCatalog().getVisualStyle("SPADEVisualStyle");
                if (spadeVS != null) {
                    // Overwrite visual style, only way to get Cytoscape to reliably update
                    cyVMM.getCalculatorCatalog().removeVisualStyle("SPADEVisualStyle");
                }
                spadeVS = new VisualStyle("SPADEVisualStyle");

                // Update with new calculators
                NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator();
                nodeAppCalc.setCalculator(visualMapping.createColorCalculator());
                nodeAppCalc.setCalculator(visualMapping.createSizeCalculator());
                spadeVS.setNodeAppearanceCalculator(nodeAppCalc);

                // Set a few defaults now that we have overwritten the calculators
                VisualPropertyType.NODE_SHAPE.setDefault(spadeVS, cytoscape.visual.NodeShape.ELLIPSE);
                VisualPropertyType.NODE_FILL_COLOR.setDefault(spadeVS, Color.LIGHT_GRAY);
                spadeVS.getDependency().set(VisualPropertyDependency.Definition.NODE_SIZE_LOCKED,true);

                cyVMM.getCalculatorCatalog().addVisualStyle(spadeVS);
                cyVMM.setVisualStyle(spadeVS);
                Cytoscape.getCurrentNetworkView().setVisualStyle(spadeVS.getName());

            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(null, "Visual Mapping Error: " + e);
            }
         }


        private void plotStyleComboActionPerformed(java.awt.event.ActionEvent evt) {
            (new drawScatterThread()).execute();
        }

        private void colorrangeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
             mapSizeAndColors();
             Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
        }

        private void colorscaleComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
            mapSizeAndColors();
            Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
        }

        /**
         * Closes the WEST CytoSPADE pane
         * @param evt
         */
        private void closeButtonWestClicked(java.awt.event.ActionEvent evt) {
            int returnvalue = JOptionPane.showConfirmDialog(null, "Close SPADE plug-in?", "Confirm close", JOptionPane.OK_CANCEL_OPTION);
            if(returnvalue == JOptionPane.OK_OPTION) {
                saveLandscaping(true);
                //FIXME This will fail if the user loads another plug-in after loading SPADE
                Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).remove(Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).getCytoPanelComponentCount()-1);
                return;
            } else {
                return;
            }
        }

        private SPADEContext spadeCxt;
        private VisualMapping visualMapping;

        private String xChanScale;
        private String yChanScale;
        private String xChanParam;
        private String yChanParam;

        //West panel controls
        private javax.swing.JComboBox plotStyleComboBox;
        private javax.swing.JComboBox colorscaleComboBox;
        private javax.swing.JComboBox filenameComboBox;
        private javax.swing.JComboBox colorrangeComboBox;
        private javax.swing.JLabel jLabelPlot; // contains the rendered plot
        private javax.swing.JLabel countLabel;
        private javax.swing.JLabel xAxisClickable; // Captures the axis click events
        private javax.swing.JLabel yAxisClickable;
        private javax.swing.JLayeredPane plotArea;
        private javax.swing.JPopupMenu xAxisPopup;
        private javax.swing.JPopupMenu yAxisPopup;
        private javax.swing.JMenuItem menuItem;
    }

    /**
     * This class gets attached to the menu item.
     */
    public class SPADEdraw extends CytoscapeAction {

        /**
         * Sets the text that should appear on the menu item.
         */
        public SPADEdraw() {
            super("SPADE");
        }

        /**
         * This method is called when the user selects the menu item.
         */
        public void actionPerformed(ActionEvent ae) {

            // Create the workflow wizard to walk user through setting up processing/analysis
            WorkflowWizard wf = new WorkflowWizard(Cytoscape.getDesktop());

            WorkflowWizard.PanelDescriptor intro = new WorkflowWizardPanels.Intro(spadeCxt);
            wf.registerWizardPanel(WorkflowWizardPanels.Intro.IDENTIFIER, intro);

            WorkflowWizard.PanelDescriptor cluster = new WorkflowWizardPanels.ClusterMarkerSelect(spadeCxt);
            wf.registerWizardPanel(WorkflowWizardPanels.ClusterMarkerSelect.IDENTIFIER, cluster);

            WorkflowWizard.PanelDescriptor panels = new WorkflowWizardPanels.PanelCreator(spadeCxt);
            wf.registerWizardPanel(WorkflowWizardPanels.PanelCreator.IDENTIFIER, panels);

            wf.setCurrentPanel(WorkflowWizardPanels.Intro.IDENTIFIER);
            int showModalDialog = wf.showModalDialog();


            if (showModalDialog == WorkflowWizard.CANCEL_RETURN_CODE)
                return;
            else if (showModalDialog != WorkflowWizard.FINISH_RETURN_CODE)
                JOptionPane.showMessageDialog(null, "Error occured in workflow wizard.");


            if (spadeCxt.getWorkflowKind() == SPADEContext.WorkflowKind.ANALYSIS) {
                //Create a tab panel for SPADE controls
                CytoPanelImp ctrlPanel = (CytoPanelImp) Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST);
                SpadePanel spadePanel = new SpadePanel(spadeCxt);
                ctrlPanel.add("SPADE", spadePanel);

                //Set the focus on the panel
                Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).setSelectedIndex(
                        Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).getCytoPanelComponentCount()-1
                );

                //This setPrefferedSize(getSize + 1), setPrefferedSize(getSize - 1)
                //is seemingly required to prevent violent behavior of the pack
                //method and to force pack to actually relayout the components
                Cytoscape.getDesktop().setPreferredSize(new Dimension(
                        Cytoscape.getDesktop().getSize().width + 1,
                        Cytoscape.getDesktop().getSize().height + 1
                        ));
                Cytoscape.getDesktop().setPreferredSize(new Dimension(
                        Cytoscape.getDesktop().getSize().width - 1,
                        Cytoscape.getDesktop().getSize().height - 1
                        ));
                Cytoscape.getDesktop().pack();
            }

            /*
            //This stupid setPrefferedSize(getSize + 1), setPrefferedSize(getSize - 1)
            //is seemingly required to prevent violent behavior of the pack
            //method and to force pack to actually relayout the components
            Cytoscape.getDesktop().setPreferredSize(new Dimension(
                    Cytoscape.getDesktop().getSize().width + 1,
                    Cytoscape.getDesktop().getSize().height + 1
                    ));
           Cytoscape.getDesktop().setPreferredSize(new Dimension(
                    Cytoscape.getDesktop().getSize().width - 1,
                    Cytoscape.getDesktop().getSize().height - 1
                    ));
            Cytoscape.getDesktop().pack();


            }
            */


            /*
            JOptionPane.showMessageDialog(null, "To author runSPADE file, please select directory containing input FCS files.\nTo analyze SPADE data, please select SPADE output directory.");

            //Select the directory of FCS and GML files
            JFileChooser FCSfileChooser = new JFileChooser();
            FCSfileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = FCSfileChooser.showOpenDialog(FCSfileChooser);
            if(returnValue == JFileChooser.APPROVE_OPTION) {
                FCSFilePath = FCSfileChooser.getSelectedFile().getPath();
            } else if (returnValue == JFileChooser.CANCEL_OPTION) {
                return;
            } else {
                JOptionPane.showMessageDialog(null, "File selection error");
                return;
            }
            

            File dir = new File(FCSFilePath);
            FilenameFilter filterAnyFCS = new FilenameFilter() {
                public boolean accept(File f, String name) {
                    return (name.endsWith(".fcs"));
                }
            };
            FilenameFilter filterDensityClusterFCS = new FilenameFilter() {
                public boolean accept(File f, String name) {
                    return (name.endsWith(".density.fcs.cluster.fcs"));
                }
            };
            FilenameFilter filtermediansGML = new FilenameFilter() {
                public boolean accept(File f, String name) {
                    return (name.endsWith(".medians.gml"));
                }
            };

            //Our list of files as array: {{FCS file, GML file}, <...>}
            File[] flAllFCS = null;
            File[] flDensityClusterFCS = null;
            File[] flGML = null;
            flAllFCS = dir.listFiles(filterAnyFCS);
            flDensityClusterFCS = dir.listFiles(filterDensityClusterFCS);
            flGML = dir.listFiles(filtermediansGML);
            int totalNumFiles = dir.listFiles().length;

            if (flAllFCS.length == 0) {
                JOptionPane.showMessageDialog(null, "Not all required files found in selected directory.");
                return;
            }

            if (flGML.length > 0) { //Analysis
                files = new File[flDensityClusterFCS.length+1][2];
                //Positions of GML files that correspond to an FCS file
                int mediansGMLPosition = 0;

                for(int i = 0; i < flDensityClusterFCS.length; i++) {
                    mediansGMLPosition = 0;
                    while(!flGML[mediansGMLPosition].getName().contains(flDensityClusterFCS[i].getName())) {
                        mediansGMLPosition++;
                        if (mediansGMLPosition > totalNumFiles) {
                            JOptionPane.showMessageDialog(null, "Failed to find medians.gml files.");
                            return;
                        }
                    }
                    files[i][0] = flDensityClusterFCS[i];
                    files[i][1] = flGML[mediansGMLPosition];
                }

                //This is for the combobox of files that the user can select to open.
                filenames = new String[files.length];
                //Start i at 1 so that the first spot is blank and a file does not load on starting the plugin.
                //Remember to implement i-1 when accessing the file list based on the filenames index.
                for(int i=0; i < files.length-1; i++) {
                    filenames[i+1] = files[i][0].getName().split(".density.fcs.cluster.fcs")[0];
                }
            }

            fcsFile FCSInputFile = null;

            if (flGML.length == 0 && flAllFCS.length > 0) { //We're in author mode; check all the FCS file params

                //Have a gander at the first file to get the list of parameters
                try {
                    FCSInputFile = new fcsFile(flAllFCS[0].getAbsolutePath(), false);
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "File not found: " + ex);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error reading file: " + ex);
                }

                //Get the full list of channels
                int numChannels = FCSInputFile.getChannelCount();
                channels   = new String[numChannels];
                channelsPN = new String[numChannels];
                for(int i=0; i < numChannels; i++) {
                    channels[i] = FCSInputFile.getChannelShortName(i)+":"+FCSInputFile.getChannelName(i);
                    channelsPN[i] = FCSInputFile.getChannelShortName(i);
                }

                //Prepare the list of filenames for the list box
                filenames = new String[flAllFCS.length];
                filenames[0] = flAllFCS[0].getName();

                //Check that the parameters in subsequent files are consistent
                for(int i = 1; i < flAllFCS.length; i++) {
                    filenames[i] = flAllFCS[i].getName();
                    //Open the file
                    try {
                        FCSInputFile = new fcsFile(flAllFCS[i].getAbsolutePath(), false);
                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(null, "File not found: " + ex);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error reading file: " + ex);
                    }

                    for(int j=0; j < numChannels; j++) {
                        if (!channels[j].contentEquals(FCSInputFile.getChannelShortName(j)+":"+FCSInputFile.getChannelName(j))) {
                            JOptionPane.showMessageDialog(null, "Error in input files: inconsistent parameter in " +
                                    flAllFCS[i].getName());
                            return;
                        }
                    }

                }

                CytoPanelImp ctrlPanel = (CytoPanelImp) Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH);
                authorPanel ap = new authorPanel();
                ctrlPanel.add("SPADE", ap);

                Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH).setSelectedIndex(
                        Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH).getCytoPanelComponentCount()-1);

            } else { //analysis mode; numChannels - 2 to exclude "density" and "cluster" from the list used to populate the combo boxes

                //Have a gander at the first file only (save time) to get the list of parameters
                try {
                    FCSInputFile = new fcsFile(flDensityClusterFCS[0].getAbsolutePath(), false);
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "File not found: " + ex);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error reading file: " + ex);
                }

                //Get the full list of channels
                int numChannels = FCSInputFile.getChannelCount();
                channels = new String[numChannels - 2];
                channelsPN = new String[numChannels - 2];
                for(int i=0; i < numChannels - 2; i++) {
                    channels[i] = FCSInputFile.getChannelShortName(i)+":"+FCSInputFile.getChannelName(i);
                    channelsPN[i] = FCSInputFile.getChannelShortName(i);
                }

                //Initialize these so that a plot shows up the first time a file is selected
                xChanParam = channels[0];
                yChanParam = channels[0];
                xChanScale = "Log";
                yChanScale = "Log";

                // Find the global_boundaries.table file it exists, and create appropiate visual mapping
                File[] boundaryFiles = new File(FCSFilePath).listFiles(new FilenameFilter() {
                    public boolean accept(File f, String name) {
                        return (name.matches("global_boundaries.table"));
                    }
                });
                if (boundaryFiles.length == 1)
                    visualMapping = new VisualMapping(boundaryFiles[0]);
                else if (boundaryFiles.length == 0)
                    visualMapping = new VisualMapping();
                else {
                    JOptionPane.showMessageDialog(null, "Error: Found more than one global_boundaries.table file, what ever shall I do!?");
                    return;
                }
               
                //Create a tab panel for SPADE controls
                CytoPanelImp ctrlPanel = (CytoPanelImp) Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST);
                SpadePanel spadePanel = new SpadePanel();
                ctrlPanel.add("SPADE", spadePanel);

                //Set the focus on the panel
                Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).setSelectedIndex(
                        Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).getCytoPanelComponentCount()-1);
            }
            //This stupid setPrefferedSize(getSize + 1), setPrefferedSize(getSize - 1)
            //is seemingly required to prevent violent behavior of the pack
            //method and to force pack to actually relayout the components
            Cytoscape.getDesktop().setPreferredSize(new Dimension(
                    Cytoscape.getDesktop().getSize().width + 1,
                    Cytoscape.getDesktop().getSize().height + 1
                    ));
           Cytoscape.getDesktop().setPreferredSize(new Dimension(
                    Cytoscape.getDesktop().getSize().width - 1,
                    Cytoscape.getDesktop().getSize().height - 1
                    ));
            Cytoscape.getDesktop().pack();
            */
        }

    }

}