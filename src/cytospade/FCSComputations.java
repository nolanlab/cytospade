package cytospade;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import cytoscape.*;

import cytoscape.logger.CyLogger;

import giny.model.GraphPerspective;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;

import java.util.Set;

import org.apache.commons.math.MathException;

import org.apache.commons.math.stat.inference.TTestImpl;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.lang.ArrayUtils;


public class FCSComputations {

    private fcsFile fcsInputFile = null;

    public FCSComputations(File inputFile) throws FileNotFoundException, IOException {        
        this(new fcsFile(inputFile, true));
    }
    
    public FCSComputations(fcsFile inputFile) {
        fcsInputFile = inputFile;
    }
    
    public fcsFile FcsFile() {
        return fcsInputFile;
    }

    /**
     * Get selected nodes
     */
    private int[] getSelectedNodes() {
        ArrayList<CyNode> selectedNodes = new ArrayList<CyNode>();

        CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
        for (CyNode node : (Set<CyNode>) currentNetwork.getSelectedNodes()) {
            GraphPerspective nestedNetwork = node.getNestedNetwork();
            if (nestedNetwork == null) {
                selectedNodes.add(node);
            } else {
                selectedNodes.addAll(nestedNetwork.nodesList());
            }
        }

        int[] selectedNodes_i = new int[selectedNodes.size()];
        for (int i = 0; i < selectedNodes.size(); i++) {
            selectedNodes_i[i] = Integer.parseInt(selectedNodes.get(i).getIdentifier()) + 1;
        }

        return selectedNodes_i;
    }

    /**
     * Populates selectedEvents
     */
    private Array2DRowRealMatrix populateSelectedEvents(
            Array2DRowRealMatrix events,
            int[] selectedClust,
            int clusterColumn) {
        ArrayList<Integer> columns = new ArrayList<Integer>();

        //Populate selectedEventsInitial                        
        for (int i = 0; i < events.getColumnDimension(); i++) {
            int cluster = (int) events.getEntry(clusterColumn, i);
            for (int j = 0; j < selectedClust.length; j++) {
                if (cluster == selectedClust[j]) {
                    columns.add(i);
                }
            }
        }

        int[] rows = new int[events.getRowDimension()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }

        return (Array2DRowRealMatrix) events.getSubMatrix(rows, ArrayUtils.toPrimitive(columns.toArray(new Integer[0])));
    }

    public class nameValuePair implements Comparable {

        public double value;
        public String name;

        public nameValuePair(String name, double value) {
            this.name = name;
            this.value = value;
        }

        public int compareTo(Object t) {
            nameValuePair rhs = (nameValuePair) t;
            if (this.value < rhs.value) {
                return -1;
            } else if (this.value > rhs.value) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public class Result {

        // Return values
        public double[] dataAx;
        public double[] dataAy;
        public double[] datax;
        public double[] datay;
        public double xChanMax;
        public double yChanMax;
        public int eventCount;
        public int numNodesSelected;
        public ArrayList<nameValuePair> pValues;

        public Result() {
        }
    }

    /**
     * T-distribution between selected nodes and all nodes
     */
    private double tTest(
            Array2DRowRealMatrix selectedEvents,
            Array2DRowRealMatrix allEvents,
            int attribute) {

        TTestImpl tTest = new TTestImpl();
        try {
            return tTest.tTest(selectedEvents.getDataRef()[attribute], allEvents.getDataRef()[attribute]);
        } catch (IllegalArgumentException ex) {
            CyLogger.getLogger(SpadeController.class.getName()).error(null, ex);
        } catch (MathException ex) {
            CyLogger.getLogger(SpadeController.class.getName()).error(null, ex);
        }
        return 1.0;
    }

    /**
     * Populates the data[A]{X,Y} arrays based on the selected file and the
     * selected node(s). (Uses only global variables.)
     */
    public Result compute(String xChanParamIn, String yChanParamIn) {

        //Get the selected nodes
        int[] selectedClust = getSelectedNodes();

        //Pull the events list
        double[][] events = fcsInputFile.getCompensatedEventList();
        int eventCount = fcsInputFile.getEventCount();
        int numNodesSelected = selectedClust.length;

        Result result = new Result();
        result.numNodesSelected = numNodesSelected;
        result.eventCount = eventCount;

        //Find the columns with the appropriate parameters
        int xChan = fcsInputFile.getChannelIdFromShortName(xChanParamIn);
        if (xChan < 0) {
            xChan = 0;
        }

        int yChan = fcsInputFile.getChannelIdFromShortName(yChanParamIn);
        if (yChan < 0) {
            yChan = 0;
        }

        result.xChanMax = fcsInputFile.getChannelRange(xChan);
        result.yChanMax = fcsInputFile.getChannelRange(yChan);
        
        //The cluster channel is always the last
        int clustChan = fcsInputFile.getChannelCount() - 1;


        if (numNodesSelected == 0) {
            result.datax = events[xChan];
            result.datay = events[yChan];
            //TODO: Do we need to assign dataAx and dataAy?

            result.numNodesSelected = 0;
        } else {
            Array2DRowRealMatrix eventsInitl = new Array2DRowRealMatrix(events);
            Array2DRowRealMatrix eventsSlctd = populateSelectedEvents(
                    eventsInitl,
                    selectedClust,
                    fcsInputFile.getChannelIdFromShortName("cluster"));

            ArrayList<nameValuePair> pValues = new ArrayList<nameValuePair>();
            for (int i = 0; i < fcsInputFile.getNumChannels(); i++) {
                String name = fcsInputFile.getChannelShortName(i);
                if (name.contentEquals("Time") || name.contentEquals("time") || name.contentEquals("cluster") || name.contentEquals("density")) {
                    continue;
                }

                pValues.add(new nameValuePair(name, tTest(eventsSlctd, eventsInitl, i)));
            }
            Collections.sort(pValues);

            result.dataAx = eventsInitl.getDataRef()[xChan];  // Background events
            result.dataAy = eventsInitl.getDataRef()[yChan];
            result.datax = eventsSlctd.getDataRef()[xChan];  // Foreground events
            result.datay = eventsSlctd.getDataRef()[yChan];
            result.eventCount = eventsSlctd.getColumnDimension();
            result.pValues = pValues;
        }

        return result;
    }
}
