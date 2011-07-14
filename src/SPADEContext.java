
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Container for SPADE state, script generation for backend, etc.
 *
 * @author mlinderm
 */
public class SPADEContext {

    public enum WorkflowKind { PROCESSING, ANALYSIS }

    static public class AnalysisPanel {
        public File[]   panel_files;
        public Object[] median_markers;
        public File[]   reference_files;
        public String[] fold_markers;

        public AnalysisPanel(Object[] panel_files, Object[] median_markers, Object[] reference_files, Object[] fold_markers) {
            this.panel_files = new File[panel_files.length];
            for (int i=0; i<panel_files.length; i++) { this.panel_files[i] = (File)panel_files[i]; }

            if (median_markers == null)
                this.median_markers = null;
            else {
                this.median_markers = new File[median_markers.length];
                for (int i=0; i<panel_files.length; i++) { this.median_markers[i] = (File)median_markers[i]; }
            }

            this.reference_files = new File[reference_files.length];
            for (int i=0; i<reference_files.length; i++) { this.reference_files[i] = (File)reference_files[i]; }

            this.fold_markers = new String[fold_markers.length];
            for (int i=0; i<fold_markers.length; i++) { this.fold_markers[i] = (String)fold_markers[i]; }
        }
    };


    private File path;
    private WorkflowKind workflowKind;

    private File[] fcsFiles;
    private File[] gmlFiles;

    private String[] selectedClusteringMarkers;

    private HashMap analysisPanels = new HashMap();

    private int arcsinh          = 5;
    private int targetClusters   = 200;
    private int targetDownsample = 5000;
    private double nodeSizeScaleFactor = 1.2;

    /**
     * @return the path
     */
    public File getPath() {
        return path;
    }

    /**
     * Sets the path for the current analysis or processing run. That analysis
     * or processing is being performed is determined during 'setting' by the
     * presence of "median" GML files in the directory pointed to by argument
     * 
     * @param path the path to set
     */
    public void setPath(File path) {
        this.path = path;

        // Flush any previously existing path data
        this.analysisPanels.clear();
        this.selectedClusteringMarkers = null;

        // Determine analysis kind
        gmlFiles = path.listFiles(new FilenameFilter() {
            public boolean accept(File f, String name) {
                return (name.endsWith(".medians.gml"));
            }
        });
        
        if (gmlFiles.length > 0) { // We are probably doing analysis
            fcsFiles = path.listFiles(new FilenameFilter() {
                public boolean accept(File f, String name) {
                    return (name.endsWith(".cluster.fcs"));
                }
            });

            // Validate that we have matching GML and FCS files
            if (gmlFiles.length != fcsFiles.length)
                throw new IllegalArgumentException("Missing fcs or gml files");

            Arrays.sort(fcsFiles);
            Arrays.sort(gmlFiles);

            for (int i=0; i<gmlFiles.length; i++) {
                if (!gmlFiles[i].getName().contains(fcsFiles[i].getName())) {
                    throw new IllegalArgumentException("Missing counterpart for "+gmlFiles[i]);
                }
            }

            // Set workflow
            workflowKind = WorkflowKind.ANALYSIS;
        } else {
            fcsFiles = path.listFiles(new FilenameFilter() {
                public boolean accept(File f, String name) {
                    return (name.endsWith(".fcs"));
                }
            });
            if (fcsFiles.length == 0)
                throw new IllegalArgumentException("No FCS files found in directory");
            Arrays.sort(fcsFiles);
            workflowKind = WorkflowKind.PROCESSING;
        }
    }

   /**
    * @return the workflowKind
    */
    public WorkflowKind getWorkflowKind() {
        return workflowKind;
    }

    /**
     * @return the fcsFiles
     */
    public File[] getFCSFiles() {
        return fcsFiles;
    }

    /**
     * @return the FCS files not currently used in any analysis panel
     */
    public File[] getFCSFilesNotInPanel() {
        HashSet f = new HashSet(Arrays.asList(fcsFiles));
        Iterator it = analysisPanels.entrySet().iterator();
        while (it.hasNext()) {
            AnalysisPanel p = (AnalysisPanel)(((Map.Entry)it.next()).getValue());
            f.removeAll(Arrays.asList(p.panel_files));
        }
        File[] files = (File[])f.toArray(new File[0]);
        Arrays.sort(files);
        return files;
    }

    /**
     * @return the gmlFiles
     */
    public File[] getGMLFiles() {
        return gmlFiles;
    }

    /**
     * @return the potentialClusteringMarkers
     */
    public String[] getPotentialClusteringMarkers() {
        return SPADEContext.getCommonMarkers(fcsFiles);
    }

    /**
     * @return the selectedClusteringMarkers
     */
    public String[] getSelectedClusteringMarkers() {
        return selectedClusteringMarkers;
    }

    /**
     * @param selectedClusteringMarkers the selectedClusteringMarkers to set
     */
    public void setSelectedClusteringMarkers(String[] selectedClusteringMarkers) {
        this.selectedClusteringMarkers = selectedClusteringMarkers;
    }

    /**
     * @return the arcsinh
     */
    public int getArcsinh() {
        return arcsinh;
    }

    /**
     * @param arcsinh the arcsinh to set
     */
    public void setArcsinh(int arcsinh) {
        this.arcsinh = arcsinh;
    }

    public double getNodeSizeScaleFactor() {
        return nodeSizeScaleFactor;
    }

    public void setNodeSizeScaleFactor(double nodeSizeScaleFactor) {
        this.nodeSizeScaleFactor = nodeSizeScaleFactor;
    }
    
    /**
     * @return the targetClusters
     */
    public int getTargetClusters() {
        return targetClusters;
    }

    /**
     * @param targetClusters the targetClusters to set
     */
    public void setTargetClusters(int targetClusters) {
        this.targetClusters = targetClusters;
    }

    /**
     * @return the targetDownsample
     */
    public int getTargetDownsample() {
        return targetDownsample;
    }

    /**
     * @param targetDownsample the targetDownsample to set
     */
    public void setTargetDownsample(int targetDownsample) {
        this.targetDownsample = targetDownsample;
    }

    public void addAnalysisPanel(String name, AnalysisPanel panel) {
        analysisPanels.put(name, panel);
    }


    public void removeAnalysisPanel(String name) {
        analysisPanels.remove(name);
    }

    public AnalysisPanel getAnalysisPanel(String name) {
        return (AnalysisPanel)analysisPanels.get(name);
    }

    public String[] getAnalysisPanelsNames() {
        return (String[])(analysisPanels.keySet().toArray(new String[0]));
    }


    /**
     * Generates formatted listing of processing to be performed. Only
     * relevant when in processing mode.
     *
     * @return pretty-printed context
     */
    public String getContextAsFormattedString() {
        StringBuilder str = new StringBuilder();
        str
            .append("Directory: ").append(this.getPath()).append("\n")
            .append("Clustering Parameters:\n")
            .append("  Arcsinh Cofactor:  ").append(this.getArcsinh()).append("\n")
            .append("  Target Downsampled Cells:  ").append(this.getTargetDownsample()).append("\n")
            .append("  Target Number of Clusters:  ").append(this.getTargetClusters()).append("\n")
            .append("  Clustering Markers:  ").append(SPADEContext.join(Arrays.asList(this.getSelectedClusteringMarkers()), ", ")).append("\n")
            .append("Panels:\n")
            ;
        if (analysisPanels.isEmpty()) {
            str
                .append("  Default:\n")
                .append("    Panel Files:  All\n")
                .append("    Median Markers:  All\n")
                .append("    Reference Files:  None\n")
                .append("    Fold-change Markers:  None\n")
                ;
        } else {
            Iterator it = analysisPanels.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry me = (Map.Entry)it.next();
                AnalysisPanel p = (AnalysisPanel)(me.getValue());
                str
                    .append("  ").append(me.getKey()).append(":\n")
                    .append("    Panel Files:  ").append(SPADEContext.join(Arrays.asList(p.panel_files),", ")).append("\n")
                    .append("    Median Markers:  All\n")
                    .append("    Reference Files:  ").append(SPADEContext.join(Arrays.asList(p.reference_files),", ")).append("\n")
                    .append("    Fold-change Markers:  ").append(SPADEContext.join(Arrays.asList(p.fold_markers),", ")).append("\n")
                    ;
            }
        }
        return str.toString();
    }

    /**
     * Generate short R script for generating plots for SPADE results in
     * this context's path. Only relevant for analysis contexts.
     *
     * @param filename the filename to write script to
     * @throws IOException
     */
    public void authorPlotSpade(String filename) throws IOException {
        FileWriter fstream;
        fstream = new FileWriter(new File(this.getPath(), filename).getAbsolutePath());

        BufferedWriter out = new BufferedWriter(fstream);
        out.write("LIBRARY_PATH=NULL\n"
        + "library(\"spade\",lib.loc=LIBRARY_PATH)\n");
        out.write(String.format("NODE_SIZE_SCALE_FACTOR=%f\n",this.getNodeSizeScaleFactor()));
        out.write("OUTPUT_DIR=\"./\"\n"
        + "LAYOUT_TABLE <- read.table(paste(OUTPUT_DIR,\"layout.table\",sep=\"\"))\n"
        + "MST_GRAPH <- read.graph(paste(OUTPUT_DIR,\"mst.gml\",sep=\"\"),format=\"gml\")\n"
        + "SPADE.plot.trees(MST_GRAPH,OUTPUT_DIR,file_pattern=\"*fcs*Rsave\",layout=as.matrix(LAYOUT_TABLE),out_dir=paste(OUTPUT_DIR,\"pdf\",sep=\"\"),size_scale_factor=NODE_SIZE_SCALE_FACTOR)\n"
        );
        out.close();
    }

    /**
     * Generate fully documented script for running SPADE over FCS files in
     * this context's path. Only relevant for processing contexts.
     * 
     * @param filename
     * @throws IOException
     */
    public void authorRunSpade(String filename) throws IOException {
        FileWriter fstream;
        fstream = new FileWriter(new File(this.getPath(), filename).getAbsolutePath());

        BufferedWriter out = new BufferedWriter(fstream);
        // <editor-fold defaultstate="collapsed" desc="runSPADE header">
        out.write("#!/usr/bin/env Rscript\n"
        + "# ^^set this to your Rscript path\n"
        + "#\n"
        + "# runSPADE:  R wrapper script for SPADE tree construction\n"
        + "# Erin Simonds - esimonds@stanford.edu\n"
        + "# Version 2.4 - November 10, 2010\n"
        + "# Autogenerated by CytoSPADE\n"
        + "#\n"
        + "# Command line instructions:\n"
        + "#   1) Make sure the first line of this file is your Rscript path (found in the same directory as R)\n"
        + "#\n"
        + "#   2) Move the entire directory containing this script and the FCS file(s) to be analyzed to the computer on which you will run SPADE\n"
        + "#\n"
        + "#   3) In a command shell, navigate to the folder containing this script and the FCS file(s) to be analyzed\n"
        + "#\n"
        + "#   4) Make this script executable.  At the command line, run:\n"
        + "#	$ chmod +x runSPADE.R\n"
        + "#\n"
        + "#   5a) For normal use (not in a load sharing or compute cluster): At the command line, run:\n"
        + "#	$ ./runSPADE.R [-num_threads=X] [-file_to_process=Y]\n"
        + "#	where X is the number of threads you wish to use (default = 1)\n"
        + "#	Y is the name of the file to process (default = use file(s) specified in this script)\n"
        + "#		Note that parameters in brackets may be omitted.\n"
        + "#\n"
        + "#   5b) For Sun Gridengine: At the command line, run:\n"
        + "#	$ qsub -cwd -j y -b y -m e -M username@domain.ext [-pe threaded A] ./runSPADE.R [-num_threads=X] [-file_to_process=Y]\n"
        + "#	where username@domain.ext is your e-mail address to e-mail when the job is done,\n"
        + "#	A is the number of slots to reserve with Gridengine,\n"
        + "#	X is the number of threads to use in SPADE (usually the same as A) (default = 1).\n"
        + "#	Y is the name of the file to process (default = use file(s) specified in this script)\n"
        + "#		Note that parameters in brackets may be omitted.\n"
        + "#\n"
        + "#   5c) For Platform LSF: At the command line, run:\n"
        + "#	$ bsub [-n A -R \"span[hosts=1]\"] ./runSPADE.R [-num_threads=X] [-file_to_process=Y]\n"
        + "#	A is the number of slots to reserve with LSF,\n"
        + "#	X is the number of threads to use in SPADE (usually the same as A) (default = 1).\n"
        + "#	Y is the name of the file to process (default = use file(s) specified in this script)\n"
        + "#		Note that parameters in brackets may be omitted. (Note that the brackets surrounding \"hosts=1\" do not indicate an optional parameter.)\n"
        + "#\n"
        + "# Interactive instructions:\n"
        + "#   1) Move the entire directory containing this script and the FCS file(s) to be analyzed to the computer on which you will run SPADE\n"
        + "#   2) In an interactive R session, change the working directory to the folder created in step 1\n"
        + "#   3) At the R command line, run: source(\"runSPADE.R\")\n"
        + "#\n");
        // </editor-fold>

        out.write("#BEGIN AUTOGENERATED DATA\n");
        out.write("FILE_TO_PROCESS=\".\"\n");

        out.write("CLUSTERING_MARKERS=c(");
        for (int i=0; i<this.selectedClusteringMarkers.length; i++) {
            if (i > 0)
                out.write(',');
            String m = this.selectedClusteringMarkers[i];
            out.write(String.format("\"%s\"",getShortNameFromFormattedName(m)));

        }
        out.write(")\n");


        if (!analysisPanels.isEmpty()) {
            out.write("PANELS=list(\n");
            Iterator it = analysisPanels.entrySet().iterator();
            while (it.hasNext()) {
                out.write("list(\n");
                AnalysisPanel p = (AnalysisPanel)(((Map.Entry)it.next()).getValue());

                out.write("panel_files=c(");
                for (int i = 0; i < p.panel_files.length; i++) {
                    if (i > 0)
                        out.write(",");
                    out.write("\"" + p.panel_files[i].getName() + "\"");
                }
                out.write("),\n");

                out.write("median_cols=NULL,\n");

                if (p.reference_files.length > 0) {
                    out.write("reference_files=c(");
                    for (int i = 0; i < p.reference_files.length; i++) {
                        if (i > 0)
                            out.write(",");
                        out.write("\""+ p.reference_files[i].getName() +"\"");
                    }
                    out.write("),\n");
                } else
                  out.write("reference_files=NULL,\n");


                if (p.fold_markers.length > 0) {
                    out.write("fold_cols=c(");
                    for (int i = 0; i < p.fold_markers.length; i++) {
                        if (i > 0)
                            out.write(",");
                        String m = p.fold_markers[i];
                        out.write(String.format("\"%s\"",getShortNameFromFormattedName(m)));
                    }
                    out.write(")\n");
                } else
                  out.write("fold_cols=NULL\n");

                if (it.hasNext())
                    out.write("),\n");
                else
                    out.write(")");
            }
            out.write(")\n");


        } else {
            out.write("PANELS=NULL\n");
        }

        out.write(String.format("ARCSINH_COFACTOR=%d\n",this.getArcsinh()));
        out.write(String.format("DOWNSAMPLED_EVENTS=%d\n",this.getTargetDownsample()));
        out.write(String.format("TARGET_CLUSTERS=%d\n",this.getTargetClusters()));

        out.write("CLUSTERING_SAMPLES=50000\n");
        out.write("DOWNSAMPLING_EXCLUDE_PCTILE=0.01\n");
        out.write("NODE_SIZE_SCALE_FACTOR=1.2\n");
        out.write("NORMALIZE=\"global\"\n");
        out.write("OUTPUT_DIR=\"output/\"\n");

        out.write("#END AUTOGENERATED DATA\n");

        out.write("LIBRARY_PATH=NULL\n"
        + "NUM_THREADS <- 1\n"
        + "for (e in commandArgs()) {\n"
        + "	ta <- strsplit(e,\"=\",fixed=TRUE)\n"
        + "	if( ta[[1]][1] == \"-num_threads\") {\n"
        + "		NUM_THREADS <- ta[[1]][2]\n"
        + "	}\n"
        + "	if( ta[[1]][1] == \"-file_to_process\") {\n"
        + "		FILE_TO_PROCESS <- ta[[1]][2]\n"
        + "	}\n"
        + "}\n"
        + "Sys.setenv(\"OMP_NUM_THREADS\"=NUM_THREADS)\n"
        + "library(\"spade\",lib.loc=LIBRARY_PATH)\n"
        + "LAYOUT_FUNCTION=layout.kamada.kawai\n"
        + "SPADE.driver(FILE_TO_PROCESS, file_pattern=\"*.fcs\", out_dir=OUTPUT_DIR, cluster_cols=CLUSTERING_MARKERS, panels=PANELS, arcsinh_cofactor=ARCSINH_COFACTOR, layout=LAYOUT_FUNCTION, downsampling_samples=DOWNSAMPLED_EVENTS, downsampling_exclude_pctile=DOWNSAMPLING_EXCLUDE_PCTILE, k=TARGET_CLUSTERS, clustering_samples=CLUSTERING_SAMPLES)\n"
        + "LAYOUT_TABLE <- read.table(paste(OUTPUT_DIR,\"layout.table\",sep=\"\"))\n"
        + "MST_GRAPH <- read.graph(paste(OUTPUT_DIR,\"mst.gml\",sep=\"\"),format=\"gml\")\n"
        + "SPADE.plot.trees(MST_GRAPH,OUTPUT_DIR,file_pattern=\"*fcs*Rsave\",layout=as.matrix(LAYOUT_TABLE),out_dir=paste(OUTPUT_DIR,\"pdf\",sep=\"\"),size_scale_factor=NODE_SIZE_SCALE_FACTOR)\n"
        + "Sys.unsetenv(\"OMP_NUM_THREADS\")\n"
        );

        out.close();
    }

    /**
     *
     * @param fcs_files
     * @return formatted channel names for channels in all input fcs_files
     */
    static public String[] getCommonMarkers(Object[] fcs_files) {
        HashSet common_markers = null;
        for (Object f : fcs_files) {
            fcsFile fcs;
            try {
                fcs = new fcsFile(((File)f).getAbsolutePath(), false);
            } catch (IOException e) {
                // TODO log errors
                continue;
            }

            HashSet file_markers = new HashSet();
            for (int i=0; i<fcs.getNumChannels(); i++) {
                file_markers.add(getFCSChannelFormattedName(fcs,i));
            }

            if (common_markers == null)
                common_markers = file_markers;
            else // Make common markers the intersection of the available markers
                common_markers.retainAll(file_markers);
        }
        String[] markers = (String[])common_markers.toArray(new String[0]);
        Arrays.sort(markers);
        return markers;
    }

    static public String getFCSChannelFormattedName(fcsFile fcs, int channel_id) {
        String desc = fcs.getChannelName(channel_id);
        return fcs.getChannelShortName(channel_id) + (desc.isEmpty() ? "" : ("::" + desc));
    }

    static public String getShortNameFromFormattedName(String name) {
        int divider = name.indexOf(':');
        return (divider == -1) ? name : name.substring(0, divider);
    }

    private static String join( Iterable< ? extends Object > pColl, String separator ) {
        Iterator< ? extends Object > oIter;
        if ( pColl == null || ( !( oIter = pColl.iterator() ).hasNext() ) ) return "";
        StringBuilder oBuilder = new StringBuilder( String.valueOf( oIter.next() ) );
        while ( oIter.hasNext() ) oBuilder.append( separator ).append( oIter.next() );
        return oBuilder.toString();
    }

}
