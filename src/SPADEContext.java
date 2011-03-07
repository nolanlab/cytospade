
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mlinderm
 */
public class SPADEContext {

    public enum WorkflowKind { PROCESSING, ANALYSIS }

    static public class AnalysisPanel {
        public File[] panel_files;
        public Object[] median_markers;
        public Object[] reference_files;
        public Object[] fold_markers;

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

            this.fold_markers = new File[fold_markers.length];
            for (int i=0; i<fold_markers.length; i++) { this.fold_markers[i] = (File)fold_markers[i]; }
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

    /**
     * @return the path
     */
    public File getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(File path) {
        this.path = path;

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

    public String[] getAnalysisPanelsNames() {
        return (String[])(analysisPanels.keySet().toArray(new String[0]));
    }

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
                file_markers.add(fcs.getChannelShortName(i)+"::"+fcs.getChannelName(i));
            }

            if (common_markers == null)
                common_markers = file_markers;
            else // Make common markers the intersection of the available markers
                common_markers.retainAll(file_markers);
        }
        return (String[])common_markers.toArray(new String[0]);
    }

}
